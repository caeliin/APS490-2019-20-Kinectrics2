#include "Bluetooth.h"
#include "SDcard.h"
#include "RTC.h"

/* UNO Circuit connections
 * SD card:
 *  10 SS
 *  11 MOSI
 *  12 MISO
 *  13 SCK
 * BT module:
 *  2 RX (board)
 *  3 my (board)
 * RTC:
 *  SDA SDA
 *  SCL SCL
 */

 /* DUE Circuit connections
 * SD card:
 *  10 SS
 *  SPI-4 MOSI
 *  SPI-1 MISO
 *  SPI-3 SCK
 * BT module:
 *  RX2 RX (board)
 *  TX2 TX (board)
 * RTC:
 *  SDA1 SDA
 *  SCL1 SCL
 */

DS3231 rtc(SDA1, SCL1); //initialize RTC

File serverFile;

Time nextRun;

void setup() {
  Serial.begin(115200);
  Serial2.begin(115200);
  
  while (!Serial) {
    ; //wait for serial port to connect
  }

  /*initialize RTC*/
  rtc.begin();
  
  /*// <- uncomment to set date and time
  rtc.setTime(1, 59, 0);
  rtc.setDate(14, 3, 2020);
  SD.remove("2000.txt");  
  //*/

  //Serial.print("Initializing SD card...");

  if (!SD.begin()) {
    Serial.println("Initialization failed!");
    return;
  }
  Serial.println("\tInitialization successful.");

  nextRun = rtc.getTime();
}

void loop() {
  if (isRunTime(nextRun)) {
    recordSensor();
    nextRun = getTimeToRunNext(0, 0, 0, 0, 0, 10);
    /*
    Serial.print("\n");
    printTime(rtc.getTime());
    Serial.print("\t");
    printTime(nextRun);
    Serial.print("\n");
    */
  }

  if (Serial2.available())
    processBT();

  Serial.print(".");
  delay(100);
}
