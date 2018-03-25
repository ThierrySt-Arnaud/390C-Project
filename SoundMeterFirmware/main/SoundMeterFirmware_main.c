
#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdatomic.h>
#include <math.h>
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

#define SPP_TAG "SoundMeterFirmware"
#define SPP_SERVER_NAME "SMF_SPP_SERVER"
#define EXAMPLE_DEVICE_NAME "SoundMeter"

#define STORAGE_NAMESPACE "BLOB_SPACE"
#define BLOB_PREFIX "set-"
#define NUMBER_OF_BLOBS "blob-tracker"
#define BLOB_SIZE 1024
#define AUDIO_SAMPLE_MEM 4096
#define AUDIO_PROCESS_MEM 4096
#define DATA_RECORDER_MEM 4096

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
static atomic_uint data_to_write = ATOMIC_VAR_INIT(0);

static TaskHandle_t sample_audio_handle = NULL;
static TaskHandle_t process_audio_handle = NULL;
static TaskHandle_t record_data_handle = NULL;

static uint32_t bt_handle = 0;
static esp_adc_cal_characteristics_t *adc_chars;

void sample_audio(void*);
void process_audio(void*);
void record_data(void*);
static void esp_spp_cb(esp_spp_cb_event_t, esp_spp_cb_param_t*);
bool initialize_bluetooth(void);
static void check_efuse();
static void print_char_val_type(esp_adc_cal_value_t);
void nvs_full(esp_err_t*);
void reset_requested(esp_err_t*);

void app_main() {
    gpio_set_direction(GPIO_NUM_5, GPIO_MODE_DEF_OUTPUT);
    gpio_set_level(GPIO_NUM_5, 0);
    gpio_set_direction(GPIO_NUM_0, GPIO_MODE_DEF_INPUT);

    esp_err_t err = nvs_flash_init();

    if (!gpio_get_level(GPIO_NUM_0)){
        reset_requested(&err);
    }

    if (!initialize_bluetooth()){
        ESP_LOGI(SPP_TAG,"Restarting...");
        esp_restart();
    }

    if (err == ESP_ERR_NVS_NO_FREE_PAGES){
        nvs_full(&err);
    }

    gpio_set_level(GPIO_NUM_5, 0);
    ESP_ERROR_CHECK( err );

    BaseType_t sample_audio_type;
    sample_audio_type = xTaskCreate(sample_audio, "AudioSampler",
                                     AUDIO_SAMPLE_MEM, (void*) 1,
                                     configMAX_PRIORITIES < 1,
                                     &sample_audio_handle);
}

static void esp_spp_cb(esp_spp_cb_event_t event, esp_spp_cb_param_t *param){
    switch (event) {
        // SPP Profile initialization event
        case ESP_SPP_INIT_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_INIT_EVT");
            esp_bt_dev_set_device_name(EXAMPLE_DEVICE_NAME);
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
            bool record_status = atomic_load(&recording);
            atomic_store(&recording, !record_status);
            vTaskResume(sample_audio_handle);
            break;

        // SPP congestion status change event
        case ESP_SPP_CONG_EVT:
            ESP_LOGI(SPP_TAG, "ESP_SPP_CONG_EVT cong=%d handle=%d", param->cong.cong, param->cong.handle);
            atomic_store(&connection_ready, !param->cong.cong);
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

bool initialize_bluetooth(){
    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    if (esp_bt_controller_init(&bt_cfg) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s initialize controller failed\n", __func__);
        return false;
    }

    if (esp_bt_controller_enable(ESP_BT_MODE_CLASSIC_BT) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s enable controller failed\n", __func__);
        return false;
    }

    if (esp_bluedroid_init() != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s initialize bluedroid failed\n", __func__);
        return false;
    }

    if (esp_bluedroid_enable() != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s enable bluedroid failed\n", __func__);
        return false;
    }

    if (esp_spp_register_callback(esp_spp_cb) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s spp register failed\n", __func__);
        return false;
    }

    if (esp_spp_init(esp_spp_mode) != ESP_OK) {
        ESP_LOGE(SPP_TAG, "%s spp init failed\n", __func__);
        return false;
    }
    return true;
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
                                     configMAX_PRIORITIES < 2,
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
    uint8_t* write_buffer = malloc(BLOB_SIZE*sizeof(uint8_t));
    BaseType_t record_data_type;
    record_data_type = xTaskCreate(record_data, "DataRecorder",
                                     DATA_RECORDER_MEM, write_buffer,
                                     configMAX_PRIORITIES < 2,
                                     &record_data_handle);
    while(true){
        vTaskSuspend(NULL);
        uint64_t readings = I2S_READ_LEN;
        //printf("Processing %llu readings...\n", readings);
        memcpy(process_buffer,buffer,I2S_READ_LEN*sizeof(uint16_t));
        uint64_t total_holder = 0;
        for (int i = 0; i < readings; i++){
            total_holder += ((process_buffer[i]-2048)*(process_buffer[i]-2048));
        }
        //printf("Total holder is: %llu\n",total_holder);
        uint64_t mean_square = (double) total_holder/readings;
        //printf("Mean square is: %llu\n", mean_square);
        double adc_rms_value = sqrt((double) mean_square);
        //printf("ADC RMS value: %f\n",adc_rms_value);
        double voltage_rms_value = adc_rms_value*3.3/4096*1.5;
        //printf("Compensated voltage RMS value: %f\n",voltage_rms_value);
        double log_value = 20*log10(voltage_rms_value/MIC_VPA);
        //printf("DB value: %f\n", log_value);
        uint8_t processed_value = (uint8_t)((log_value-39.0)/14.867262*256);
        printf("Processed: %i\n", processed_value);
        if (atomic_load(&connection_open) && atomic_load(&connection_ready)){
            esp_spp_write(bt_handle, sizeof(uint8_t), &processed_value);
        }
        if (atomic_load(&recording)){
            uint16_t data_in_buffer = atomic_load(&data_to_write);
            write_buffer[data_in_buffer++] = processed_value;
            printf("Recorded value #%i\n", data_in_buffer);
            atomic_store(&data_to_write, data_in_buffer);
            if (data_in_buffer == BLOB_SIZE){
                printf("We have %i values, preparing for write to flash.\n", data_in_buffer);
                vTaskResume(record_data_handle);
            }
        }
    }
}

void record_data(void* write_buffer){
    uint8_t* ready_buffer = malloc(BLOB_SIZE*sizeof(uint8_t));
    while(true){
        vTaskSuspend(NULL);
        uint16_t to_write = atomic_load(&data_to_write);
        if (to_write > 0){
            printf("Will write %i to flash\n", to_write);
            nvs_handle my_handle;
            esp_err_t err;
            // Open
            err = nvs_open(STORAGE_NAMESPACE, NVS_READWRITE, &my_handle);
            if (err != ESP_OK)
                ESP_LOGE(SPP_TAG, "%s nvs open failed err=%s\n", __func__, esp_err_to_name(err));

            uint16_t blobs_in_tow = 0;
            err = nvs_get_u16(my_handle, NUMBER_OF_BLOBS, &blobs_in_tow);
            if (err == ESP_ERR_NVS_NOT_FOUND){
                ESP_LOGI(SPP_TAG, "No blob in storage.");
                err = nvs_set_u16(my_handle, NUMBER_OF_BLOBS, blobs_in_tow);
            }
            if (err != ESP_OK){
                ESP_LOGE(SPP_TAG, "%s couldn't get blob tracker err=%s\n", __func__, esp_err_to_name(err));
            } else {
                char blob_prefix[] = BLOB_PREFIX;
                char* blob_key = malloc(15*sizeof(char));
                sprintf(blob_key,"%s%i",blob_prefix,++blobs_in_tow);
                printf("Blob key is: %s\n", blob_key);
                err = nvs_set_blob(my_handle, blob_key, ready_buffer, (to_write*sizeof(uint8_t)));
                if (err != ESP_OK){
                    ESP_LOGE(SPP_TAG, "%s new blob write failed err=%s\n", __func__, esp_err_to_name(err));
                } else {
                    err = nvs_set_u16(my_handle, NUMBER_OF_BLOBS, blobs_in_tow);
                    if (err != ESP_OK)
                        ESP_LOGE(SPP_TAG, "%s blob tracker update failed err=%s\n", __func__, esp_err_to_name(err));
                }


                err = nvs_commit(my_handle);
                if (err != ESP_OK)
                    ESP_LOGE(SPP_TAG, "%s nvs commit failed err=%s\n", __func__, esp_err_to_name(err));
                free(blob_key);
            }
            if (err == ESP_OK)
                atomic_store(&data_to_write, 0);
            nvs_close(my_handle);
        }
    }
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
