#include <DS3231.h>
#include <Wire.h>
#include <calib.h>
#include <dmm.h>
#include <dmmcmd.h>
#include <DMMShield_mod.h>
#include <eprom.h>
#include <errors.h>
#include <gpio.h>
#include <serialno.h>
#include <spi.h>
#include <utils.h>

#include <SoftwareSerial.h>

SoftwareSerial DMMModule(5, 7); //RX | TX

int LEDpin = 6;
int BTon = 8;

DMMShield_mod dmmShieldObj;
uint8_t bErrCode = 0;

DS3231 Clock;

void setup() {
  // Start the serial port
  Serial.begin(9600);
  DMMModule.begin(9600);
  pinMode(LEDpin, OUTPUT);
  pinMode(BTon, INPUT);

  while (!Serial) {
    ; //wait for serial port to connect
  }

  //---------------------------------------------------------RTC---------------------------------------------------------
  // Start the I2C interface
  Wire.begin();

  // Alarm is not enabled! Should set alarm
  if (!Clock.checkAlarmEnabled(1))
  {
    Clock.setClockMode(false);
    // 0b1111 // each second
    // 0b1110 // Once per minute (when second matches)
    // 0b1100 // Once per hour (when minute and second matches)
    // 0b1000 // Once per day (when hour, minute and second matches)
    // 0b0000 // Once per month when date, hour, minute and second matches. Once per week if day of the week and A1Dy=true
    // Set alarm to happen every minute (change to your wanted interval)
    Clock.setA1Time(1, 1, 1, 0, 0b1110, false, false, false);
    Clock.turnOnAlarm(1);
  }

  //---------------------------------------------------------DMM SENSOR INIT---------------------------------------------------------
  dmmShieldObj.begin(&DMMModule);
  Serial.println("DMMShield Library Basic Commands demo");
  dmmShieldObj.ProcessIndividualCmd("DMMSetScale VoltageDC5");
  //  bErrCode = dmmShieldObj.SetScale(8);  //"5 V DC" scale
  if (bErrCode == 0)
  {
    // success
    Serial.println("5 V DC Scale");
  }
  //---------------------------------------------------------DMM SENSOR RUN---------------------------------------------------------
  char szMsg[20];
  digitalWrite(LEDpin, HIGH);
      delay(1000);                       // wait one second
  bErrCode = dmmShieldObj.GetFormattedValue(szMsg);
  if (bErrCode == 0)
  {
    // success
    Serial.print("Value = ");
    Serial.println(szMsg);
  }
  bErrCode = dmmShieldObj.GetFormattedValue(szMsg);
  if (bErrCode == 0)
  {
    // success
    Serial.print("Value = ");
    Serial.println(szMsg);
  }
  bErrCode = dmmShieldObj.GetFormattedValue(szMsg);
  if (bErrCode == 0)
  {
    // success
    Serial.print("Value = ");
    Serial.println(szMsg);
  }

  delay(10000);
  //---------------------------------------------------------END CODE---------------------------------------------------------
  // Reset alarm to turn off the device
  Clock.checkIfAlarm(1);
}

void loop() {
  // nothing in loop so it doesn't stay on
}
