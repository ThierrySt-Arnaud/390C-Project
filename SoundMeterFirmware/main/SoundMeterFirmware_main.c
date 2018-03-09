
#include <stdint.h>
#include <string.h>
#include <stdbool.h>
#include <stdio.h>
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

#define SPP_TAG "SoundMeterFirmware"
#define SPP_SERVER_NAME "SMF_SPP_SERVER"
#define EXAMPLE_DEVICE_NAME "SoundMeter"

#define DUMMY_VALUES 10
#define DUMMY_DATA 48, 49, 50, 51, 52, 53, 54, 55, 56, 57
#define STORAGE_NAMESPACE "dummy"
#define DUMMY_KEY "dummy_data"
#define DUMMY_SENDER_SIZE 1024

static const esp_spp_sec_t sec_mask = ESP_SPP_SEC_NONE;
static const esp_spp_role_t role_slave = ESP_SPP_ROLE_SLAVE;
static const esp_spp_mode_t esp_spp_mode = ESP_SPP_MODE_CB;

static BaseType_t send_dummy_data_type;
static TaskHandle_t send_dummy_data_handle = NULL;

esp_err_t get_dummy_data(uint8_t*, size_t*);
void send_dummy_data(void*);
esp_err_t print_dummy_data(void);
static void esp_spp_cb(esp_spp_cb_event_t, esp_spp_cb_param_t*);
bool initialize_bluetooth(void);
esp_err_t save_dummy_data(int8_t[]);

void app_main() {
  ESP_ERROR_CHECK(nvs_flash_erase());
  esp_err_t err = nvs_flash_init();
  ESP_ERROR_CHECK( err );

  gpio_set_direction(GPIO_NUM_5, GPIO_MODE_DEF_OUTPUT);
  gpio_set_level(GPIO_NUM_5, 0);

  int8_t dummyData[] = {DUMMY_DATA};

  err = save_dummy_data(dummyData);
  if (err != ESP_OK)
    ESP_LOGE(SPP_TAG,"%s while saving dummy data to NVS\n", esp_err_to_name(err));

  if (!initialize_bluetooth()){
    ESP_LOGI(SPP_TAG,"Restarting...");
    esp_restart();
  }
}

void send_dummy_data(void* handle){
  ESP_LOGI(SPP_TAG, "send_dummy_data task created");
  uint32_t* bt_handle = (uint32_t*) handle;
  TickType_t xLastWakeTime;
  const TickType_t xPeriod = pdMS_TO_TICKS(125);
  uint8_t i = 0;
  size_t dummyValues = 0;
  uint8_t* dummyData = NULL;
  esp_err_t err = ESP_OK;

  get_dummy_data(dummyData, &dummyValues);

  while(err == ESP_OK){
    err = esp_spp_write(*bt_handle, sizeof(dummyData[i]), &dummyData[i++]);
    i %= dummyValues;
    vTaskDelayUntil(&xLastWakeTime, xPeriod);
  }

  free(dummyData);
  return;
}

esp_err_t get_dummy_data(uint8_t* dummyData, size_t* dummyValues){
  nvs_handle my_handle;
  esp_err_t err;

  free(dummyData);
  dummyData = NULL;

  // Open
  err = nvs_open(STORAGE_NAMESPACE, NVS_READONLY, &my_handle);
  if (err != ESP_OK) return err;

  err = nvs_get_blob(my_handle, DUMMY_KEY, dummyData, dummyValues);
  if (err != ESP_OK) return err;

  dummyData = malloc(*dummyValues);

  err = nvs_get_blob(my_handle, DUMMY_KEY, dummyData, dummyValues);
  if (err != ESP_OK) return err;

  // Close
  nvs_close(my_handle);
  return ESP_OK;
}

esp_err_t print_dummy_data(void){
  esp_err_t err;
  size_t dummyValues = 0;
  uint8_t* dummyData = NULL;

  err = get_dummy_data(dummyData, &dummyValues);
  if (err != ESP_OK) return err;

  printf("There are %i values. They are:\n", dummyValues);

  for (size_t i = 0; i < dummyValues; i++) {
    printf("%i\n", dummyData[i]);
  }

  free(dummyData);
  return ESP_OK;
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
      vTaskDelete(send_dummy_data_handle);
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
      break;

    // SPP congestion status change event
    case ESP_SPP_CONG_EVT:
      ESP_LOGI(SPP_TAG, "ESP_SPP_CONG_EVT cong=%d", param->cong.cong);
      if (param->cong.cong == 0) {
        uint32_t* handle = &param->cong.handle;
        send_dummy_data_type = xTaskCreate(send_dummy_data,
                                           "DataSender",
                                           DUMMY_SENDER_SIZE,
                                           handle,
                                           tskIDLE_PRIORITY,
                                           send_dummy_data_handle);
      } else{
        vTaskDelete(send_dummy_data_handle);
      }
      break;

    // SPP Write event
    case ESP_SPP_WRITE_EVT:
      ESP_LOGI(SPP_TAG, "ESP_SPP_WRITE_EVT len=%d cong=%d", param->write.len , param->write.cong);
      break;

    // SPP server connection opening event
    case ESP_SPP_SRV_OPEN_EVT:
      ESP_LOGI(SPP_TAG, "ESP_SPP_SRV_OPEN_EVT");
      uint32_t* handle = &param->srv_open.handle;
      send_dummy_data_type = xTaskCreate(send_dummy_data,
                                         "DataSender",
                                         DUMMY_SENDER_SIZE,
                                         handle,
                                         tskIDLE_PRIORITY,
                                         send_dummy_data_handle);
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

esp_err_t save_dummy_data(int8_t dummyData[]){
  nvs_handle my_handle;
  esp_err_t err;

  // Open
  err = nvs_open(STORAGE_NAMESPACE, NVS_READWRITE, &my_handle);
  if (err != ESP_OK) return err;

  err = nvs_set_blob(my_handle, "dummy_data", dummyData, (DUMMY_VALUES*sizeof(int8_t)));
  if (err != ESP_OK) return err;

  err = nvs_commit(my_handle);
  if (err != ESP_OK) return err;

  nvs_close(my_handle);
  return ESP_OK;
}
