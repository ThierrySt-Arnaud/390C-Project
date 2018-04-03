
#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdatomic.h>
#include <math.h>
#include <sys/unistd.h>
#include <sys/stat.h>
#include "nvs.h"
#include "nvs_flash.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "esp_bt.h"
#include "esp_bt_main.h"
#include "esp_gap_bt_api.h"
#include "esp_bt_device.h"
#include "esp_spp_api.h"
#include "time.h"
#include "sys/time.h"
#include "driver/adc.h"
#include "driver/i2s.h"
#include "esp_adc_cal.h"
#include "esp_spiffs.h"

#define SPP_TAG "SoundMeterFirmware"
#define SPP_SERVER_NAME "SMF_SPP_SERVER"
#define DEVICE_NAME "SoundMeter"

#define CONFIG_NAMESPACE "config_space"
#define PROJECT_NAME "project"
#define LOCATION "location"
#define RECORDING_STATUS "recording"

#define MAX_WRITE_BUFFER 256
#define MAX_SEND_BUFFER 512
#define STORAGE_FILENAME "/spiffs/recordingdata.bin"

#define AUDIO_SAMPLE_MEM 4096
#define AUDIO_PROCESS_MEM 4096
#define DATA_RECORDER_MEM 4096
#define COMMAND_PARSER_MEM 4096
#define DATA_SENDER_MEM 4096
#define CONFIG_SENDER_MEM 4096
#define CONFIG_SAVER_MEM 4096

//i2s number
#define I2S_NUM             0
//i2s sample rate
#define I2S_SAMPLE_RATE     48000
//i2s data bits
#define I2S_SAMPLE_BITS     16
//I2S read buffer length
#define I2S_READ_LEN        I2S_SAMPLE_RATE/8
//I2S data format
#define I2S_FORMAT          I2S_CHANNEL_FMT_ONLY_LEFT
//I2S channel number
#define I2S_CHANNEL_NUM     1
//I2S built-in ADC unit
#define I2S_ADC_UNIT        ADC_UNIT_1
//I2S built-in ADC channel
#define I2S_ADC_CHANNEL     ADC1_CHANNEL_0
//I2S built-in ADC attenuation
#define I2S_ADC_ATTEN       ADC_ATTEN_DB_11

#define DEFAULT_VREF        1153 // Measured manually

#define AMP_MIC_SENSIVITY   8
#define MIC_VPA             0.005011872336273

static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_NONE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;
static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;
static atomic_bool connection_ready = ATOMIC_VAR_INIT(false);
static atomic_bool connection_open = ATOMIC_VAR_INIT(false);
static atomic_bool recording = ATOMIC_VAR_INIT(false);
static atomic_bool uploading = ATOMIC_VAR_INIT(false);
static atomic_bool downloading = ATOMIC_VAR_INIT(false);
static atomic_uint data_to_write = ATOMIC_VAR_INIT(0);

static TaskHandle_t parse_command_handle = NULL;
static TaskHandle_t send_data_handle = NULL;
static TaskHandle_t record_config_handle = NULL;
static TaskHandle_t send_config_handle = NULL;
static TaskHandle_t sample_audio_handle = NULL;
static TaskHandle_t process_audio_handle = NULL;
static TaskHandle_t record_data_handle = NULL;

static uint32_t bt_handle = 0;
static esp_adc_cal_characteristics_t *adc_chars;

typedef struct {
    char* received;
    uint16_t size;
} received_data_type;

static received_data_type* received_data = NULL;

void parse_command(void*);
void sample_audio(void*);
void process_audio(void*);
void record_data(void*);
void send_data(void*);
void send_config(void*);
void record_config(void*);
static void esp_spp_cb(esp_spp_cb_event_t, esp_spp_cb_param_t*);
esp_err_t initialize_bluetooth(void);
esp_err_t initialize_spiffs(void);
static void check_efuse();
static void print_char_val_type(esp_adc_cal_value_t);
void nvs_full(esp_err_t*);
void reset_requested(esp_err_t*);
/*esp_err_t save_recording_status(uint8_t);
uint8_t get_recording_status(void);*/

void app_main() {
    gpio_set_direction(GPIO_NUM_5, GPIO_MODE_DEF_OUTPUT);
    gpio_set_level(GPIO_NUM_5, 0);
    gpio_set_direction(GPIO_NUM_0, GPIO_MODE_DEF_INPUT);

    esp_err_t err = nvs_flash_init();
    if (err == ESP_ERR_NVS_NO_FREE_PAGES){
        nvs_full(&err);
    } else if(err != ESP_OK){
        gpio_set_level(GPIO_NUM_5, 1);
        while(gpio_get_level(GPIO_NUM_0)){}
        err = nvs_flash_erase();
        err = nvs_flash_init();
    }

    ESP_ERROR_CHECK( err );

    if (!gpio_get_level(GPIO_NUM_0)){
        reset_requested(&err);
    }

    ESP_ERROR_CHECK( err );

    err = initialize_bluetooth();
    if (err != ESP_OK){
        ESP_LOGE(SPP_TAG,"Unable to initialize Bluetooth err=%s", esp_err_to_name(err));
        esp_restart();
    }

    err = initialize_spiffs();
    if (err != ESP_OK){
        ESP_LOGE(SPP_TAG,"Unable to initialize SPI filesystem err=%s", esp_err_to_name(err));
        esp_restart();
    }

    gpio_set_level(GPIO_NUM_5, 0);

    /*if(get_recording_status > 0){
        atomic_store(&recording,true);
    }*/

    BaseType_t sample_audio_type;
    sample_audio_type = xTaskCreate(sample_audio, "AudioSampler",
                                     AUDIO_SAMPLE_MEM, (void*) NULL,
                                     configMAX_PRIORITIES << 1,
                                     &sample_audio_handle);
    BaseType_t parse_command_type;
    parse_command_type = xTaskCreate(parse_command, "CommandParser",
                                     COMMAND_PARSER_MEM, (void*) NULL,
                                     (configMAX_PRIORITIES << 2)-1,
                                     &parse_command_handle);

}

static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param){
    switch (event) {
        // SPP Profile initialization event
        case ESP_SPP_INIT_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_INIT_EVT");
            esp_bt_dev_set_device_name(DEVICE_NAME);
            esp_bt_gap_set_scan_mode(ESP_BT_SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            esp_spp_start_srv(sec_mask,role_slave, 0, SPP_SERVER_NAME);
            break;

        // Discovery event
        case ESP_SPP_DISCOVERY_COMP_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_DISCOVERY_COMP_EVT");
            break;

        // Connection initiation event
        case ESP_SPP_OPEN_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_OPEN_EVT");
            break;

        // Connection closure event
        case ESP_SPP_CLOSE_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_CLOSE_EVT");
            atomic_store(&connection_open, false);
            break;

        // SPP profile start event
        case ESP_SPP_START_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_START_EVT");
            break;

        // SPP Client initialization event
        case ESP_SPP_CL_INIT_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_CL_INIT_EVT");
            break;

        // SPP Incoming data event
        case ESP_SPP_DATA_IND_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_DATA_IND_EVT len=%d handle=%d",
                   param->data_ind.len, param->data_ind.handle);
            esp_log_buffer_hex("",param->data_ind.data,param->data_ind.len);
            received_data = malloc(sizeof(received_data_type));
            received_data->received = malloc(param->data_ind.len);
            memcpy(received_data->received,param->data_ind.data,param->data_ind.len);
            received_data->size = param->data_ind.len;
            if(!atomic_load(&downloading)){
                vTaskResume(parse_command_handle);
            } else{
                vTaskResume(record_config_handle);
            }
            break;

        // SPP congestion status change event
        case ESP_SPP_CONG_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_CONG_EVT cong=%d handle=%d", param->cong.cong, param->cong.handle);
            atomic_store(&connection_ready, !param->cong.cong);
            if(!param->cong.cong && atomic_load(&uploading)){
                vTaskResume(send_config_handle);
            }
            break;

        // SPP Write event
        case ESP_SPP_WRITE_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_WRITE_EVT len=%d cong=%d", param->write.len , param->write.cong);
            atomic_store(&connection_ready, !param->write.cong);
            break;

        // SPP server connection opening event
        case ESP_SPP_SRV_OPEN_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_SRV_OPEN_EVT handle=%d",param->srv_open.handle);
            atomic_store(&connection_ready, true);
            atomic_store(&connection_open, true);
            bt_handle = param->srv_open.handle;
            vTaskResume(sample_audio_handle);
            break;

        default:
            break;
    }
}

esp_err_t initialize_bluetooth(){
    esp_err_t err = ESP_OK;
    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();

    err = esp_bt_controller_init(&bt_cfg);
    if (err != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s initialize controller failed", __func__);
        return err;
    }

    err = esp_bt_controller_enable(ESP_BT_MODE_CLASSIC_BT);
    if (err != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s enable controller failed", __func__);
        return err;
    }

    err = esp_bluedroid_init();
    if (err != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s initialize bluedroid failed", __func__);
        return err;
    }

    err = esp_bluedroid_enable();
    if (err != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s enable bluedroid failed", __func__);
        return err;
    }

    err = esp_spp_register_callback(esp_spp_cb);
    if (err != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s spp register failed", __func__);
        return err;
    }

    err = esp_spp_init(esp_spp_mode);
    if (err != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s spp init failed", __func__);
        return err;
    }
    return err;
}

esp_err_t initialize_spiffs(){
    esp_vfs_spiffs_conf_t conf = {
      .base_path = "/spiffs",
      .partition_label = NULL,
      .max_files = 5,
      .format_if_mount_failed = true
    };

    esp_err_t ret = esp_vfs_spiffs_register(&conf);

    if (ret != ESP_OK) {
        if (ret == ESP_FAIL) {
            ESP_LOGE(SPP_TAG, "Failed to mount or format filesystem");
        } else if (ret == ESP_ERR_NOT_FOUND) {
            ESP_LOGE(SPP_TAG, "Failed to find SPIFFS partition");
        } else {
            ESP_LOGE(SPP_TAG, "Failed to initialize SPIFFS");
        }
        return ret;
    }

    size_t total = 0, used = 0;
    ret = esp_spiffs_info(NULL, &total, &used);

    if (ret != ESP_OK) {
        ESP_LOGE(SPP_TAG, "Failed to get SPIFFS partition informatio");
        return ret;
    } else {
        ESP_LOGI(SPP_TAG, "Partition size: total: %d, used: %d", total, used);
    }

    return ret;
}

void parse_command(void* parameters){
    char upload_comm[] = "{{{";
    char download_comm[] = "}}}";
    char record_comm[] = "###";
    char confirm[] = "OK";
    BaseType_t send_config_type;
    BaseType_t record_config_type;
    while(true){
        vTaskSuspend(NULL);
        printf("Parsing...\n");
        char* received = received_data->received;
        uint16_t size = received_data->size;
        free(received_data);
        printf("Received: %s\n", received);
        if (size >= 3){
            if (strncmp(upload_comm+(size-3),received,3) == 0){
                esp_spp_write(bt_handle,2,(uint8_t*)confirm);
                atomic_store(&recording,false);
                vTaskResume(record_data_handle);
                atomic_store(&uploading,true);
                send_config_type = xTaskCreate(send_config, "ConfigSender",
                                               CONFIG_SENDER_MEM, (void*) NULL,
                                               configMAX_PRIORITIES < 1,
                                               &send_config_handle);
                printf("Starting upload.\n");
            } else if (strncmp(download_comm+(size-3),received,3) == 0){
                esp_spp_write(bt_handle,2,(uint8_t*)confirm);
                record_config_type = xTaskCreate(record_config, "ConfigSaver",
                                                 CONFIG_SAVER_MEM, (void*) NULL,
                                                 configMAX_PRIORITIES < 2,
                                                 &record_config_handle);
                printf("Starting download.\n");
            } else if (strncmp(record_comm+(size-3),received,3) == 0){
                bool rec_status = atomic_load(&recording);
                rec_status = !rec_status;
                atomic_store(&recording, rec_status);
                if (!rec_status){
                    vTaskResume(record_data_handle);
                }
                esp_spp_write(bt_handle,2,(uint8_t*)confirm);
                printf("Changed recording status.\n");
            }
        }
        free(received);
    }
}

void sample_audio(void* params){
    check_efuse();

    adc1_config_width(ADC_WIDTH_BIT_12);
    adc1_config_channel_atten(I2S_ADC_CHANNEL, I2S_ADC_ATTEN);
    //Characterize ADC
    adc_chars = calloc(1, sizeof(esp_adc_cal_characteristics_t));
    esp_adc_cal_value_t val_type = esp_adc_cal_characterize(I2S_ADC_UNIT, I2S_ADC_ATTEN, ADC_WIDTH_BIT_12, DEFAULT_VREF, adc_chars);
    print_char_val_type(val_type);
    adc_set_data_inv(I2S_ADC_UNIT, true);

    i2s_config_t i2s_config = {
       .mode = I2S_MODE_MASTER | I2S_MODE_RX | I2S_MODE_ADC_BUILT_IN,
       .sample_rate =  I2S_SAMPLE_RATE,
       .bits_per_sample = I2S_SAMPLE_BITS,
       .communication_format = I2S_COMM_FORMAT_I2S_LSB,
       .channel_format = I2S_FORMAT,
       .intr_alloc_flags = 0,
       .dma_buf_count = 8,
       .dma_buf_len = 1024,
       .use_apll = false
    };

    //install and start i2s driver
    i2s_driver_install(I2S_NUM, &i2s_config, 0, NULL);
    //init ADC pad
    i2s_set_adc_mode(I2S_ADC_UNIT, I2S_ADC_CHANNEL);

    uint16_t* reading_buffer = malloc(I2S_READ_LEN*sizeof(int16_t));
    TickType_t record_delay = pdMS_TO_TICKS(125);

    BaseType_t process_audio_type;
    process_audio_type = xTaskCreate(process_audio, "AudioProcessor",
                                     AUDIO_PROCESS_MEM, reading_buffer,
                                     configMAX_PRIORITIES << 2,
                                     &process_audio_handle);
    while (true){
        while (!atomic_load(&connection_open) && !atomic_load(&recording)){
            i2s_adc_disable(I2S_NUM);
            vTaskResume(record_data_handle);
            vTaskSuspend(NULL);
            i2s_adc_enable(I2S_NUM);
        }
        int16_t bytesRead = i2s_read_bytes(I2S_NUM,(char*)reading_buffer, I2S_READ_LEN*sizeof(uint16_t), record_delay);
        if (bytesRead == I2S_READ_LEN*2){
            /*printf("4 samples out of %i:\n", I2S_READ_LEN);
            //printf("%i\n",reading_buffer[0]);
            //printf("%i\n",reading_buffer[I2S_READ_LEN/4]);
            printf("%i\n",reading_buffer[I2S_READ_LEN/2]);
            printf("%i\n",reading_buffer[3*I2S_READ_LEN/4]);*/
            vTaskResume(process_audio_handle);
        }
    }
}

void process_audio(void* buffer){
    int16_t* process_buffer = malloc(I2S_READ_LEN*sizeof(uint16_t));
    uint8_t* write_buffer = malloc(MAX_WRITE_BUFFER*sizeof(uint8_t));
    BaseType_t record_data_type;
    record_data_type = xTaskCreate(record_data, "DataRecorder",
                                     DATA_RECORDER_MEM, write_buffer,
                                     configMAX_PRIORITIES << 3,
                                     &record_data_handle);
    while(true){
        vTaskSuspend(NULL);
        uint16_t readings = I2S_READ_LEN;
        //printf("Processing %llu readings...\n", readings);
        memcpy(process_buffer,buffer,I2S_READ_LEN*sizeof(uint16_t));
        uint64_t total_holder = 0;
        uint64_t calverage = 0;
        for (int i = 0; i < readings; i++){
            calverage += process_buffer[i];
        }
        calverage /= readings;
        printf("Calibration average: %llu\n",calverage);
        for (int i = 0; i < readings; i++){
            total_holder += ((process_buffer[i]-calverage)*(process_buffer[i]-calverage));
        }
        double adc_rms_value = sqrt((double) total_holder/readings);
        printf("ADC RMS value: %f\n",adc_rms_value);
        double voltage_rms_value = adc_rms_value*3.3/4096*1.5;
        printf("Compensated voltage RMS value: %f\n",voltage_rms_value);
        double log_value = 20*log10(voltage_rms_value/MIC_VPA);
        printf("DB value: %f\n", log_value+54);
        uint8_t processed_value = (int8_t) round(((log_value+12.26779888)/66.22235685*256)-128);
        printf("Processed: %i\n", processed_value);
        if (atomic_load(&connection_open) && atomic_load(&connection_ready)){
            esp_spp_write(bt_handle, sizeof(uint8_t), &processed_value);
        }
        if (atomic_load(&recording)){
            uint16_t data_in_buffer = atomic_load(&data_to_write);
            write_buffer[data_in_buffer++] = processed_value;
            printf("Recorded value #%i\n", data_in_buffer);
            atomic_store(&data_to_write, data_in_buffer);
            if (data_in_buffer >= MAX_WRITE_BUFFER){
                printf("We have %i values, preparing for write to flash.\n", data_in_buffer);
                vTaskResume(record_data_handle);
            }
        }
    }
}

void record_data(void* write_buffer){
    uint8_t* ready_buffer = NULL;
    while(true){
        vTaskSuspend(NULL);
        uint16_t to_write = atomic_load(&data_to_write);
        ready_buffer = malloc(to_write);
        memcpy(ready_buffer,write_buffer,to_write);
        FILE* storagefile = fopen(STORAGE_FILENAME,"ab");
        if (storagefile == NULL){
            ESP_LOGE(SPP_TAG, "Failed to open file");
            esp_restart();
        } else {
            uint16_t written = fwrite(ready_buffer,sizeof(uint8_t),to_write,storagefile);
            if(written < to_write){
                ESP_LOGE(SPP_TAG,"Could only write %i of %i values!", written, to_write);
                fclose(storagefile);
                esp_restart();
            }
            atomic_store(&data_to_write,(to_write-written));
            fclose(storagefile);
        }
        free(ready_buffer);
    }
}

void record_config(void* params){
    nvs_handle my_handle;
    esp_err_t err = ESP_OK;
    err = nvs_open(CONFIG_NAMESPACE, NVS_READWRITE, &my_handle);
    if (err == ESP_OK){
        uint8_t fully_received = 0;
        char* newconfig = NULL;
        uint16_t newconfig_length = 0;
        while (fully_received < 2){
            vTaskSuspend(NULL);
            char* received = received_data->received;
            uint16_t size = received_data->size;
            free(received_data);
            char* tempconfig = malloc(newconfig_length+size);
            if (newconfig != NULL){
                memcpy(tempconfig,newconfig,newconfig_length);
                free(newconfig);
            }
            memcpy(tempconfig+newconfig_length,received,size);
            newconfig = tempconfig;
            newconfig_length += size;
            free(received);
            if(newconfig[newconfig_length-1] == '\010'){
                newconfig[newconfig_length-1] = 0;
                if (fully_received == 0){
                    err = nvs_set_str(my_handle, PROJECT_NAME, newconfig);
                } else if(fully_received == 1){
                    err = nvs_set_str(my_handle, LOCATION, newconfig);
                }
                free(newconfig);
                newconfig_length = 0;
                fully_received++;
            }
        }
        err = nvs_commit(my_handle);
        if (err != ESP_OK){
            ESP_LOGE(SPP_TAG,"Error committing values err=%s", esp_err_to_name(err));
        }
        nvs_close(my_handle);
    }

    atomic_store(&downloading,false);
    vTaskDelete(NULL);
}

void send_config(void* params){
    nvs_handle my_handle;
    esp_err_t err;

    // Open
    err = nvs_open(CONFIG_NAMESPACE, NVS_READONLY, &my_handle);
    if (err != ESP_OK){
        ESP_LOGE(SPP_TAG, "%s open NVS failed err=%s", __func__, esp_err_to_name(err));
    } else {
        char* config_to_send = NULL;
        size_t config_length = 0;
        err = nvs_get_str(my_handle, PROJECT_NAME, config_to_send, &config_length);
        if(err == ESP_OK){
            config_to_send = malloc(config_length);
            err = nvs_get_str(my_handle, PROJECT_NAME, config_to_send, &config_length);
            if(err == ESP_OK){
                config_to_send[config_length-1] = '\010';
                if(!atomic_load(&connection_open)){
                    ESP_LOGE(SPP_TAG, "Connection closed before upload completion.");
                    nvs_close(my_handle);
                    free(config_to_send);
                    atomic_store(&uploading,false);
                    vTaskDelete(NULL);
                }
                while(!atomic_load(&connection_ready)){vTaskSuspend(NULL);}
                esp_spp_write(bt_handle,config_length,(uint8_t*) config_to_send);
            }
            free(config_to_send);
        }

        err = nvs_get_str(my_handle, LOCATION, config_to_send, &config_length);
        if(err == ESP_OK){
            config_to_send = malloc(config_length);
            err = nvs_get_str(my_handle, LOCATION, config_to_send, &config_length);
            if(err == ESP_OK){
                config_to_send[config_length-1] = '\010';
                if(!atomic_load(&connection_open)){
                    ESP_LOGE(SPP_TAG, "Connection closed before upload completion.");
                    nvs_close(my_handle);
                    free(config_to_send);
                    atomic_store(&uploading,false);
                    vTaskDelete(NULL);
                }
                while(!atomic_load(&connection_ready)){vTaskSuspend(NULL);}
                esp_spp_write(bt_handle,config_length,(uint8_t*) config_to_send);
            }
            free(config_to_send);
        }

        char close_config[] = "\026";
        if(!atomic_load(&connection_open)){
            ESP_LOGE(SPP_TAG, "Connection closed before upload completion.");
            nvs_close(my_handle);
            atomic_store(&uploading,false);
            vTaskDelete(NULL);
        }
        while(!atomic_load(&connection_ready)){vTaskSuspend(NULL);}
        esp_spp_write(bt_handle,1,(uint8_t*) close_config);

        nvs_close(my_handle);
        BaseType_t send_data_type = xTaskCreate(send_data, "DataSender",
                                                DATA_SENDER_MEM, (void*) NULL,
                                                configMAX_PRIORITIES < 1,
                                                &send_data_handle);
        while(true){
            vTaskSuspend(NULL);
            vTaskResume(send_data_handle);
        }
    }
    vTaskDelete(NULL);
}

void send_data(void * params){
    char open_data[]= "<<<";
    if(!atomic_load(&connection_open)){
        ESP_LOGE(SPP_TAG, "Connection closed before upload completion.");
        atomic_store(&uploading,false);
        vTaskDelete(send_config_handle);
        vTaskDelete(NULL);
    }
    while(!atomic_load(&connection_ready)){vTaskSuspend(NULL);}
    esp_spp_write(bt_handle,sizeof(open_data),(uint8_t *) open_data);
    FILE* storagefile = fopen(STORAGE_FILENAME,"rb");
    if (storagefile == NULL){
        ESP_LOGE(SPP_TAG, "Failed to open file");
        esp_restart();
    } else{
        fseek(storagefile , 0 , SEEK_END);
        uint32_t lSize = ftell(storagefile);
        rewind(storagefile);
        uint8_t* send_buffer = malloc(MAX_SEND_BUFFER*sizeof(uint8_t));
        while(!feof(storagefile) && !ferror(storagefile)){
            uint16_t to_send = fread(send_buffer, sizeof(uint8_t), MAX_SEND_BUFFER, storagefile);
            if(!atomic_load(&connection_open)){
                ESP_LOGE(SPP_TAG, "Connection closed before upload completion.");
                free(send_buffer);
                fclose(storagefile);
                atomic_store(&uploading,false);
                vTaskDelete(send_config_handle);
                vTaskDelete(NULL);
            }
            while(!atomic_load(&connection_ready)){vTaskSuspend(NULL);}
            esp_spp_write(bt_handle,to_send*sizeof(uint8_t), send_buffer);
            lSize -= to_send*sizeof(uint8_t);
        }
        if (ferror(storagefile)){
            ESP_LOGE(SPP_TAG, "Failed to fully read file.");
        }
        free(send_buffer);
        if (lSize == 0){
            char close_data[]= ">>>\026";
            if(!atomic_load(&connection_open)){
                ESP_LOGE(SPP_TAG, "Connection closed before upload completion.");
                fclose(storagefile);
                atomic_store(&uploading,false);
                vTaskDelete(send_config_handle);
                vTaskDelete(NULL);
            }
            while(!atomic_load(&connection_ready)){vTaskSuspend(NULL);}
            esp_spp_write(bt_handle,sizeof(close_data), (uint8_t *) close_data);
            ESP_LOGI(SPP_TAG, "Upload completed.");
            fclose(storagefile);
            storagefile = fopen(STORAGE_FILENAME,"wb");
        }
        fclose(storagefile);
    }
    atomic_store(&uploading,false);
    vTaskDelete(send_config_handle);
    vTaskDelete(NULL);
}

static void check_efuse(){
    //Check TP is burned into eFuse
    if (esp_adc_cal_check_efuse(ESP_ADC_CAL_VAL_EFUSE_TP) == ESP_OK) {
        printf("eFuse Two Point: Supported\n");
    } else {
        printf("eFuse Two Point: NOT supported\n");
    }

    //Check Vref is burned into eFuse
    if (esp_adc_cal_check_efuse(ESP_ADC_CAL_VAL_EFUSE_VREF) == ESP_OK) {
        printf("eFuse Vref: Supported\n");
    } else {
        printf("eFuse Vref: NOT supported\n");
    }
}

static void print_char_val_type(esp_adc_cal_value_t val_type){
    if (val_type == ESP_ADC_CAL_VAL_EFUSE_TP) {
        printf("Characterized using Two Point Value\n");
    } else if (val_type == ESP_ADC_CAL_VAL_EFUSE_VREF) {
        printf("Characterized using eFuse Vref\n");
    } else {
        printf("Characterized using Default Vref\n");
    }
}

void nvs_full(esp_err_t* err){
    while(*err == ESP_ERR_NVS_NO_FREE_PAGES){
        TickType_t flash_wait = pdMS_TO_TICKS(125);
        gpio_set_level(GPIO_NUM_5, 1);
        vTaskDelay(flash_wait);
        if (!gpio_get_level(GPIO_NUM_0)){
            reset_requested(err);
        }
        gpio_set_level(GPIO_NUM_5, 0);
    }
}

void reset_requested(esp_err_t* err){
    TickType_t reset_wait = pdMS_TO_TICKS(500);
    gpio_set_level(GPIO_NUM_5, 1);
    vTaskDelay(reset_wait);
    if (!gpio_get_level(GPIO_NUM_0)){
        gpio_set_level(GPIO_NUM_5, 0);
        vTaskDelay(reset_wait);
        if (!gpio_get_level(GPIO_NUM_0)){
            gpio_set_level(GPIO_NUM_5, 1);
            vTaskDelay(reset_wait);
            if (!gpio_get_level(GPIO_NUM_0)){
                gpio_set_level(GPIO_NUM_5, 0);
                vTaskDelay(reset_wait);
                if (!gpio_get_level(GPIO_NUM_0)){
                    gpio_set_level(GPIO_NUM_5, 1);
                    vTaskDelay(reset_wait);
                    if (!gpio_get_level(GPIO_NUM_0)){
                        gpio_set_level(GPIO_NUM_5, 0);
                        vTaskDelay(reset_wait);
                        if (!gpio_get_level(GPIO_NUM_0)){
                            gpio_set_level(GPIO_NUM_5, 1);
                            *err = nvs_flash_erase();
                            *err = nvs_flash_init();
                            if (*err == ESP_OK){
                                ESP_LOGI(SPP_TAG,"Flash erased");
                            } else {
                                ESP_LOGE(SPP_TAG,"%s flash erase failed err=%s",__func__, esp_err_to_name(*err));
                            }
                            esp_restart();
                        }
                    }
                }
            }
        }
    }
    gpio_set_level(GPIO_NUM_5, 0);
}

/*esp_err_t save_recording_status(){
    nvs_handle my_handle;
    esp_err_t err = ESP_OK;
    err = nvs_open(CONFIG_NAMESPACE,NVS_READWRITE,&my_handle);
    if (err != ESP_OK){
        ESP_LOGE(SPP_TAG,"Error opening NVS err=%s", esp_err_to_name(err));
        return err;
    }
    err = nvs_set_u8(my_handle, RECORDING_STATUS, atomic_load(&recording));
    if (err != ESP_OK){
        ESP_LOGE(SPP_TAG,"Error saving recording status err=%s", esp_err_to_name(err));
        return err;
    }

}

uint8_t get_recording_status(){
    nvs_handle my_handle;
    esp_err_t err = ESP_OK;
    err = nvs_open(CONFIG_NAMESPACE,NVS_READONLY,&my_handle);
}*/
