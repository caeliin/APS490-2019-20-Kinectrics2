#include "SDcard.h"
#include "RTC.h"
#include <avr/dtostrf.h>

/* Sensor stuff */
const int sensor = A0;
int air = 581; //may want to convert voltage to a percentage
int water = 0; //TODO: measure this

/* Read a voltage value from the sensor */
float readSensor() {
  int sensorVal = analogRead(sensor);
  return(3.0/1024)*sensorVal;
}

/*
 * Read from the sensor, and write the full date and sensor value to the SD card
 */
void recordSensor() {
  float sensorVoltage = readSensor();
  char voltageString[5];
  //convert float to string separately - not compatable with sprintf
  dtostrf(sensorVoltage, 4, 2, voltageString);
  
  Time time = rtc.getTime();
  
  //YYYY-MM-DD HH:MM:SS V.VV //25 chars
  char formattedString[26];
  sprintf(formattedString, "%4d-%02d-%02d %02d:%02d:%02d %s", 
    time.year, time.mon, time.date, time.hour, time.min, time.sec, voltageString);

  //open the server file - keep one file per year
  char fileName[9];
  sprintf(fileName, "%4d.txt", rtc.getTime().year);
  File serverFile = SD.open(fileName, FILE_WRITE); 

  if (serverFile) {
    Serial.println(formattedString);
    serverFile.println(formattedString);
    serverFile.close();
  }
}

void readInteger(int *num, int numChars) {
  *num = 0;
  for (int i = 0; i < numChars; i++) {
    *num = *num * 10 + serverFile.read() - '0';
  }
  serverFile.read(); //consume delimiting character
}

/*
 * This moves the file cursor to the beginning of the first line where the entry is no older than the specified date
 * Year is assumed to be more recent
 */
void seekInFile(int month, int day, int hour, int minute, int second) {
  while (serverFile.available()) {
    int f_year, f_month, f_day, f_hour, f_minute, f_second;
    readInteger(&f_year, 4);
    readInteger(&f_month, 2);
    readInteger(&f_day, 2);
    readInteger(&f_hour, 2);
    readInteger(&f_minute, 2);
    readInteger(&f_second, 2);
  
    for (int i = 0; i < 6; i++) { //voltage and some extra characters
      serverFile.read();
    }
    

    if (month > f_month) {
      continue;
    }
    else if (month < f_month) {
      serverFile.seek(serverFile.position() - 26);
      return;
    }
    else if (day > f_day) {
      continue;
    }
    else if (day < f_day) {
      serverFile.seek(serverFile.position() - 26);
      return;
    }
    else if (hour > f_hour) {
      continue;
    }
    else if (hour < f_hour) {
      serverFile.seek(serverFile.position() - 26);
      return;
    }
    else if (minute > f_minute) {
      continue;
    }
    else if (minute < f_minute) {
      serverFile.seek(serverFile.position() - 26);
      return;
    }
    else if (second > f_second) {
      continue;
    }
    else if (second <= f_second) {
      serverFile.seek(serverFile.position() - 26);
      return;
    }
  }
}


/*
 * Fills in the given char array with data from serverFile
 * serverFile MUST be open already
 * return true if read successfully, false otherwise
 * char line[] = "([YYYY, MM, DD, HH, MM, SS], V.VV)";
 */
bool fillFileLine(char *line) {
  if (!serverFile.available()) {
    return false;
  }
  //year
  for (int i = 2; i < 6; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character

  //month
  for (int i = 8; i < 10; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character

  //day
  for (int i = 12; i < 14; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character

  //hour
  for (int i = 16; i < 18; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character

  //minute
  for (int i = 20; i < 22; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character

  //second
  for (int i = 24; i < 26; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character

  //voltage
  for (int i = 29; i < 33; i++) {
    line[i] = serverFile.read();
  }
  serverFile.read(); //delimiting character
  serverFile.read(); //delimiting character
  return true;
}

/* 
 * Read and parse one line from serverFile
 * serverFile MUST be open already
 * return true if read successfully, false otherwise
 */
bool readFileLine(int *year, int *month, int *day, int *hour, int *minute, int *second, float *voltage) {
  if (!serverFile.available()) {
    return false;
  }
  
  readInteger(year, 4);
  readInteger(month, 2);
  readInteger(day, 2);
  readInteger(hour, 2);
  readInteger(minute, 2);
  readInteger(second, 2);
  
  char voltage_string[6];
  for (int i = 0; i < 5; i++) {
    voltage_string[i] = serverFile.read();
  }
  voltage_string[5] = '\0';
  *voltage = atof(voltage_string);
  serverFile.read(); //newline character

  return true;  
}

/*
 * Debugging function - dumps all data on the SD card for the given year
 */
void dumpFile(int year) {
  char fileName[9];
  sprintf(fileName, "%4d.txt", year);
  //Serial.print("Opening file.. ");
  Serial.println(fileName);
  serverFile = SD.open(fileName); 
  if (serverFile) {
    Serial.println("Dumping file contents...");
    while (serverFile.available()) {
      Serial.write(serverFile.read());
    }
    Serial.println("\tDone dumping file contents");
    serverFile.close();
  }
  else {
    Serial.println("Could not open file");
  }
}
