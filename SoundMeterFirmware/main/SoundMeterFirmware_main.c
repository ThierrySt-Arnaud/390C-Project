
#include <stdio.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "esp_spi_flash.h"
#include "driver/gpio.h"
#include "nvs.h"
#include "nvs_flash.h"

#define DUMMY_VALUES 10
#define DUMMY_DATA 30, 31, 32, 33, 34, 35, 36, 37, 38, 39
#define STORAGE_NAMESPACE "dummy"

esp_err_t print_dummy_data(void){
    nvs_handle my_handle;
    esp_err_t err;

    // Open
    err = nvs_open(STORAGE_NAMESPACE, NVS_READONLY, &my_handle);
    if (err != ESP_OK) return err;

    // Read restart counter
    size_t dummyValues = 0; // value will default to 0, if not set yet in NVS
    uint8_t* dummyData = NULL;

    err = nvs_get_blob(my_handle,"dummy_data", dummyData, &dummyValues);
    if (err != ESP_OK) return err;
    dummyData = malloc(dummyValues);
    err = nvs_get_blob(my_handle,"dummy_data", dummyData, &dummyValues);
    if (err != ESP_OK) return err;

    printf("There are %i values. They are:\n", dummyValues);

    for (size_t i = 0; i < dummyValues; i++) {
      printf("%i\n", dummyData[i]);
    }

    free(dummyData);
    // Close
    nvs_close(my_handle);
    return ESP_OK;
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

void app_main() {
  ESP_ERROR_CHECK(nvs_flash_erase());

  esp_err_t err = nvs_flash_init();
  ESP_ERROR_CHECK( err );

  gpio_set_direction(GPIO_NUM_5, GPIO_MODE_DEF_OUTPUT);
  gpio_set_level(GPIO_NUM_5, 0);

  int8_t dummyData[] = {DUMMY_DATA};

  printf("In memory, dummy data is\n");
  for (int i = 0; i< sizeof(dummyData); i++)
    printf("%i\n", dummyData[i]);

  err = save_dummy_data(dummyData);
  if (err != ESP_OK) printf("Error (%s) while saving dummy data to NVS!\n",
   esp_err_to_name(err));

  while (err == ESP_OK){
    err = print_dummy_data();
    vTaskDelay(5000 / portTICK_PERIOD_MS);
  }

  printf("Error (%s) while reading dummy data to NVS!\n", esp_err_to_name(err));

  esp_restart();
}
