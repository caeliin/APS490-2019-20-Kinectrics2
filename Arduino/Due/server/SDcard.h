#ifndef SDCARD_H
#define SDCARD_H

/* SD card stuff */
#include <SPI.h>
#include <SD.h>
const int chipSelect = 10;
extern File serverFile;
extern const int sensor;

float readSensor();
void recordSensor();
void readInteger(int *num, int numChars);
void seekInFile(int month, int day, int hour, int minute, int second);
bool fillFileLine(char *line);
bool readFileLine(int *year, int *month, int *day, int *hour, int *minute, int *second, float *voltage);
void dumpFile(int year);

#endif
