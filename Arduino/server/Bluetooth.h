#ifndef BLUETOOTH_H
#define BLUETOOTH_H
#include <Arduino.h>
#include <ArduinoJson.h>
#include <SoftwareSerial.h>

extern SoftwareSerial BTmodule; //RX | TX

void processBT();
void dump();
void query(int year, int month, int day, int hour, int minute, int second);

#endif
