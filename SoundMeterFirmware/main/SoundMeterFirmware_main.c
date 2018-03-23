
#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdatomic.h>
//#include <math.h>
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
#include "esp_adc_cal.h"

#define SPP_TAG "SoundMeterFirmware"
#define SPP_SERVER_NAME "SMF_SPP_SERVER"
#define EXAMPLE_DEVICE_NAME "SoundMeter"

#define DUMMY_VALUES 10
#define DUMMY_DATA 48, 49, 50, 51, 52, 53, 54, 55, 56, 57
#define STORAGE_NAMESPACE "dummy"
#define DUMMY_KEY "dummy_data"
#define AUDIO_SAMPLE_MEM 4096
#define PROCESS_AUDIO_MEM 4096

#define MULTISAMPLE_SIZE 32
#define READING_BUFFER_SIZE 25
#define SAMPLING_FREQ 48000
#define SAMPLING_BUFFER_SIZE SAMPLING_FREQ/8

#define DC_OFFSET 1628

static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_NONE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;
static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;
static atomic_bool connection_ready = ATOMIC_VAR_INIT(false);
static atomic_bool connection_open = ATOMIC_VAR_INIT(false);
static atomic_bool recording = ATOMIC_VAR_INIT(false);

static TaskHandle_t sample_audio_handle = NULL;
static TaskHandle_t process_audio_handle = NULL;
static uint32_t bt_handle = 0;

esp_err_t get_dummy_data(uint8_t**, size_t*);
void sample_audio(void*);
void process_audio(void*);
esp_err_t print_dummy_data(void);
static void esp_spp_cb(esp_spp_cb_event_t, esp_spp_cb_param_t*);
bool initialize_bluetooth(void);
esp_err_t save_dummy_data(uint8_t[]);
uint32_t SquareRootRounded(uint32_t);

void app_main() {
    esp_err_t err = nvs_flash_init();
    if (err == ESP_ERR_NVS_NO_FREE_PAGES) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        err = nvs_flash_init();
    }
    ESP_ERROR_CHECK( err );

    gpio_set_direction(GPIO_NUM_5, GPIO_MODE_DEF_OUTPUT);
    gpio_set_level(GPIO_NUM_5, 0);

    uint8_t dummyData[] = {DUMMY_DATA};

    err = save_dummy_data(dummyData);
    if (err != ESP_OK)
    ESP_LOGE(SPP_TAG,"%s while saving dummy data to NVS\n", esp_err_to_name(err));

    ESP_ERROR_CHECK(print_dummy_data());

    if (!initialize_bluetooth()){
        ESP_LOGI(SPP_TAG,"Restarting...");
        esp_restart();
    }

    BaseType_t sample_audio_type;

    sample_audio_type = xTaskCreate(sample_audio, "AudioSampler",
                                     AUDIO_SAMPLE_MEM, (void*) 1,
                                     configMAX_PRIORITIES - 1,
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

esp_err_t print_dummy_data(void){
    esp_err_t err;
    size_t dummyValues = 0;
    uint8_t* dummyData = NULL;

    err = get_dummy_data(&dummyData, &dummyValues);
    if (err != ESP_OK) return err;

    printf("There are %i values. They are:\n", dummyValues);

    for (size_t i = 0; i < dummyValues; i++) {
        printf("%i\n", dummyData[i]);
    }

    free(dummyData);
    return ESP_OK;
}

esp_err_t save_dummy_data(uint8_t dummyData[]){
    nvs_handle my_handle;
    esp_err_t err;
    size_t required_size = 0;
    // Open
    err = nvs_open(STORAGE_NAMESPACE, NVS_READWRITE, &my_handle);
    if (err != ESP_OK) return err;

    err = nvs_get_blob(my_handle, DUMMY_KEY, NULL, &required_size);
    if (err == ESP_ERR_NVS_NOT_FOUND){
        ESP_LOGI(SPP_TAG, "Previous data not found. Writing new data.");
        err = nvs_set_blob(my_handle, DUMMY_KEY, dummyData, (DUMMY_VALUES*sizeof(uint8_t)));
        if (err != ESP_OK) return err;

        err = nvs_commit(my_handle);
        if (err != ESP_OK) return err;
    } else
        ESP_LOGI(SPP_TAG, "Previous dummy data found.");

    nvs_close(my_handle);
    return ESP_OK;
}

esp_err_t get_dummy_data(uint8_t** dummyData, size_t* dummyValues){
    nvs_handle my_handle;
    esp_err_t err;

    free(*dummyData);
    *dummyData = NULL;

    // Open
    err = nvs_open(STORAGE_NAMESPACE, NVS_READONLY, &my_handle);
    if (err != ESP_OK) return err;

    err = nvs_get_blob(my_handle, DUMMY_KEY, *dummyData, dummyValues);
    if (err != ESP_OK) return err;

    *dummyData = malloc(*dummyValues);

    err = nvs_get_blob(my_handle, DUMMY_KEY, *dummyData, dummyValues);
    if (err != ESP_OK) return err;

    // Close
    nvs_close(my_handle);
    return ESP_OK;
}

void sample_audio(void * params){
    ESP_LOGI(SPP_TAG, "Starting audio sampling task.");
    TickType_t pxPreviousWakeTime = xTaskGetTickCount();
    int16_t* reading_buffer = malloc(READING_BUFFER_SIZE*sizeof(int16_t));
    BaseType_t process_audio_type;
    process_audio_type = xTaskCreate(process_audio, "AudioProcessor",
                                    PROCESS_AUDIO_MEM, reading_buffer,
                                    configMAX_PRIORITIES >> 1,
                                    &process_audio_handle);
    esp_err_t err = ESP_OK;

    if ((err = adc1_config_width(ADC_WIDTH_BIT_12)) != ESP_OK)
        ESP_LOGE(SPP_TAG, "%s ADC initialization failed, err=%s", __func__, esp_err_to_name(err));

    if ((err = adc1_config_channel_atten(ADC_CHANNEL_7, ADC_ATTEN_DB_11)) != ESP_OK)
        ESP_LOGE(SPP_TAG, "%s ADC initialization failed, err=%s", __func__, esp_err_to_name(err));

    uint16_t iterator = 0;
    while(true) {
        /*if (atomic_load(&recording)){
            ESP_LOGI(SPP_TAG, "Recording.");
        }
        if (atomic_load(&connection_open)) {
            ESP_LOGI(SPP_TAG, "Connected.");
        }*/
        if (atomic_load(&recording) || atomic_load(&connection_open)){
            int32_t adc_reading = 0;
            for (int i = 0; i < MULTISAMPLE_SIZE; i++){
                adc_reading += adc1_get_raw(ADC_CHANNEL_7);
            }
            adc_reading >>= 5;
            //ESP_LOGI(SPP_TAG, "Multisampled reading: %i", adc_reading);
            reading_buffer[iterator++] = adc_reading;
            if (iterator == READING_BUFFER_SIZE){
                //ESP_LOGI(SPP_TAG, "Buffer is full, processing buffer.");
                vTaskResume(process_audio_handle);
                iterator = 0;
            }
            vTaskDelayUntil(&pxPreviousWakeTime,1);
        } else {
            iterator = 0;
            vTaskSuspend(NULL);
            pxPreviousWakeTime = xTaskGetTickCount();
        }
    }
}

void process_audio(void* reading_buffer){
    int16_t* process_buffer = malloc(READING_BUFFER_SIZE*sizeof(int16_t));;
    uint32_t processed_value = 0;
    while (true){
        vTaskSuspend(NULL);
        memcpy(process_buffer,reading_buffer,READING_BUFFER_SIZE*sizeof(int16_t));
        for (int i = 0; i < READING_BUFFER_SIZE; i++){
            processed_value += (process_buffer[i] - DC_OFFSET)*(process_buffer[i] - DC_OFFSET);
        }
        processed_value /= READING_BUFFER_SIZE;
        processed_value = SquareRootRounded(processed_value);

        if (atomic_load(&connection_open) && atomic_load(&connection_ready)){
            esp_spp_write(bt_handle, sizeof(uint32_t), &processed_value);
        }
    }
}

/**
 * \brief    Fast Square root algorithm, with rounding
 *
 * This does arithmetic rounding of the result. That is, if the real answer
 * would have a fractional part of 0.5 or greater, the result is rounded up to
 * the next integer.
 *      - SquareRootRounded(2) --> 1
 *      - SquareRootRounded(3) --> 2
 *      - SquareRootRounded(4) --> 2
 *      - SquareRootRounded(6) --> 2
 *      - SquareRootRounded(7) --> 3
 *      - SquareRootRounded(8) --> 3
 *      - SquareRootRounded(9) --> 3
 *
 * \param[in] a_nInput - unsigned integer for which to find the square root
 *
 * \return Integer square root of the input value.
 */
uint32_t SquareRootRounded(uint32_t a_nInput){
    uint32_t op  = a_nInput;
    uint32_t res = 0;
    uint32_t one = 1uL << 30; // The second-to-top bit is set: use 1u << 14 for uint16_t type; use 1uL<<30 for uint32_t type


    // "one" starts at the highest power of four <= than the argument.
    while (one > op) {
        one >>= 2;
    }

    while (one != 0) {
        if (op >= res + one) {
            op = op - (res + one);
            res = res +  2 * one;
        }
        res >>= 1;
        one >>= 2;
    }

    /* Do arithmetic rounding to nearest integer */
    if (op > res) {
        res++;
    }

    return res;
}
