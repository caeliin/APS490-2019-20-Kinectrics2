#include "Bluetooth.h"
#include "SDcard.h"
#include "RTC.h"

SoftwareSerial BTmodule(2, 3); //RX | TX
DS3231 rtc(SDA, SCL); //initialize RTC
File serverFile;

Time nextRun;

void setup() {
  Serial.begin(9600);
  BTmodule.begin(9600);
  
  while (!Serial) {
    ; //wait for serial port to connect
  }

  /*initialize RTC*/
  rtc.begin();
  /*// <- uncomment to set date and time
  rtc.setTime(1, 44, 0);
  rtc.setDate(20, 2, 2020);
  //*/

  //Serial.print("Initializing SD card...");

  if (!SD.begin()) {
    //Serial.println("Initialization failed!");
    return;
  }
  //Serial.println("\tInitialization successful.");

  nextRun = rtc.getTime();
}

void loop() {
  if (isRunTime(nextRun)) {
    recordSensor();
    /*
    Serial.print("\n");
    printTime(rtc.getTime());
    Serial.print("\t");
    nextRun = getTimeToRunNext(0, 0, 0, 0, 0, 10);
    printTime(nextRun);
    Serial.print("\n");
    */
  }

  if (BTmodule.available())
    processBT();

  Serial.print(".");
  delay(100);
}
