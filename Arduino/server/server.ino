/* Bluetooth stuff */
#include <ArduinoJson.h>
#include <SoftwareSerial.h>
SoftwareSerial BTmodule(2, 3); //RX | TX

/* Sensor stuff */
const int sensor = A0;
int air = 581; //may want to convert voltage to a percentage
int water = 0; //TODO: measure this

/* RTC stuff */
#include "DS3231.h"
DS3231 rtc(SDA, SCL); //initialize RTC

/* SD card stuff */
#include <SPI.h>
#include <SD.h>
const int chipSelect = 10;
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
  rtc.setTime(4, 30, 0);
  rtc.setDate(19, 2, 2020);
  //*/

  Serial.print("Initializing SD card...");

  if (!SD.begin()) {
    Serial.println("Initialization failed!");
    return;
  }
  Serial.println("\tInitialization successful.");

  //dumpFile(2020);
  nextRun = rtc.getTime();
}

void loop() {
  if (isRunTime(nextRun)) {
    //recordSensor();
    Serial.print("\n");
    printTime(rtc.getTime());
    Serial.print("\t");
    nextRun = getTimeToRunNext(0, 0, 0, 0, 0, 10);
    printTime(nextRun);
    Serial.print("\n");
  }

  if (BTmodule.available())
    processBT();

  //recordSensor(); //read from sensor and record in SD card

  Serial.print(".");
  delay(100);
}

/* Read a voltage value from the sensor */
float readSensor() {
  int sensorVal = analogRead(sensor);
  return(3.0/1024)*sensorVal;
}

/*
 * A helper function to print a time in human-readable format to the serial monitor
 */
void printTime(Time time) {
  char prettyTime[20];
  sprintf(prettyTime, "%4d-%02d-%02d %02d:%02d:%02d", 
    time.year, time.mon, time.date, time.hour, time.min, time.sec);
  Serial.print(prettyTime);
}

/*
 * Return the Time the specified amount of interval in the future
 * e.g. calling this function with day_d = 1 will return the Time one day from the present
 */
Time getTimeToRunNext(int year_d, int month_d, int day_d, int hour_d, int minute_d, int second_d) {
  Time time = rtc.getTime();
  
  time.sec += second_d;
  if (time.sec >= 60) {
    time.min += time.sec/60;
    time.sec = time.sec%60;
  }

  time.min += minute_d;
  if (time.min >= 60) {
    time.hour += time.min/60;
    time.min = time.min%60;
  }

  time.hour += hour_d;
  if (time.hour >= 24) {
    time.date += time.hour/24;
    time.hour = time.hour%24;  
  }

  time.mon += month_d;
  while (time.mon > 12) {
    time.year++;
    time.mon -= 12;
  }  

  time.year += year_d;

  time.date += day_d;
  bool dateOK = false;
  while (!dateOK) {
    int allowedDays;
    //february
    if (time.mon == 2) {
      if (time.year % 4 == 0) //leap year
        allowedDays = 29;
      else //not leap year
        allowedDays = 28;
    } 
    else if (time.mon == 4 || time.mon == 6 || time.mon == 9 || time.mon == 11) { //months with 30 days - april, june, september, november
      allowedDays = 30;
    }
    else { //all the other months
      allowedDays = 31;
    }

    if (time.date > allowedDays) {
      time.mon++;
      time.date -= allowedDays;
      if (time.mon > 12) {
        time.year++;
        time.mon -= 12;
      }
    }
    else {
      dateOK = true;
    }
  } //end check date

  return time;
}

/*
 * Check whether we're at or past the target time
 */
bool isRunTime(Time target) {
  Time now = rtc.getTime();

  if (now.year > target.year) 
    return true;
  else if (now.year < target.year) 
    return false;
  
  if (now.mon > target.mon)
    return true;
  else if (now.mon < target.mon)
    return false;

  if (now.date > target.date)
    return true;
  else if (now.date < target.date)
    return false;

  if (now.hour > target.hour)
    return true;
  else if (now.hour < target.hour)
    return false;

  if (now.min > target.min)
    return true;
  else if (now.min < target.min)
    return false;

  if (now.sec >= target.sec)
    return true;
  else 
    return false;
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
  Serial.print("Opening file.. ");
  Serial.println(fileName);
  serverFile = SD.open(fileName, FILE_WRITE); 

  if (serverFile) {
    Serial.print("Writing to file... ");
    Serial.print(formattedString);
    serverFile.println(formattedString);
    serverFile.close();
    Serial.println("\t done.");
  }
  else {
    Serial.println("Could not open file");
  }
}

/*
 * Debugging function - dumps all data on the SD card for the given year
 */
void dumpFile(int year) {
  char fileName[9];
  sprintf(fileName, "%4d.txt", year);
  Serial.print("Opening file.. ");
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
  Serial.println(voltage_string);
  *voltage = atof(voltage_string);
  serverFile.read(); //newline character

  return true;  
}

void readInteger(int *num, int numChars) {
  *num = 0;
  for (int i = 0; i < numChars; i++) {
    *num = *num * 10 + serverFile.read() - '0';
  }
  serverFile.read(); //consume delimiting character
}


/*
 * Handle messages over Bluetooth - parse the request and craft the response
 */
void processBT() {
  DynamicJsonDocument received(128);
  DeserializationError err = deserializeJson(received, BTmodule);
  if (err) {
    Serial.print("Json deserialization failed with code ");
    Serial.println(err.c_str());
    return;
  }
  
  char *req = received["request"];
  Serial.println(req);

  if (strcmp(req, "DUMP") == 0) {
    dump();
  }
  else if (strcmp(req, "QUERY") == 0) {
    int year = received["year"];
    int month = received["month"];
    int day = received["day"];
    int hour = received["hour"];
    int minute = received["minute"];
    int second = received["second"];
    query(year, month, day, hour, minute, second);
  }
  else if (strcmp(req, "END") == 0) {
    Serial.println("received END");
    BTmodule.write("{\"response\": \"EXIT\"}\n");
  }
  return;
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
 * Handle a dump request
 * Dump the SD card's files and send over BT
 */
void dump() {
  int currentYear = rtc.getTime().year;
  char fileName[9];
  
  BTmodule.write("{\"response\": \"DATA\", \"data\" : [");
  
  for (int file_year = 2000; file_year <= currentYear; file_year++) {
    sprintf(fileName, "%4d.txt", file_year);

    if (SD.exists(fileName)) {
      //Send the contents of the file over BT
      serverFile = SD.open(fileName); 
      
      bool first = true; //lists are a pain
      char line[] = "([YYYY, MM, DD, HH, MM, SS], V.VV)";
      
      while (serverFile.available()) {
        //write the comma that separates objects
        if (!first) {
          BTmodule.write(", ");
        }
        else {
          first = false;
        }
        
        //write the line for a single entry
        fillFileLine(line);
        Serial.println(line);
        BTmodule.write(line); 
      }
      serverFile.close();
    }
  }
  BTmodule.write("]}\n");
}

/*
 * Handle a query request
 * Find the first entry that is more recent than the requested date and send them over BT
 */
void query(int year, int month, int day, int hour, int minute, int second) {

  int currentYear = rtc.getTime().year;
  char fileName[9];
  
  BTmodule.write("{\"response\": \"DATA\", \"data\" : [");
  
  for (int file_year = year; file_year <= currentYear; file_year++) {
    sprintf(fileName, "%4d.txt", file_year);
    if (SD.exists(fileName)) {
      //Send the contents of the file over BT
      serverFile = SD.open(fileName); 
      
      bool first = true; //lists are a pain
      char line[] = "([YYYY, MM, DD, HH, MM, SS], V.VV)";

      if (file_year == year) {
        seekInFile(month, day, hour, minute, second); //move the cursor to the right position in the file
      }
      
      while (serverFile.available()) {
        //write the comma that separates objects
        if (!first) {
          BTmodule.write(", ");
        }
        else {
          first = false;
        }
        
        //write the line for a single entry
        fillFileLine(line);
        Serial.println(line);
        BTmodule.write(line); 
      }
      serverFile.close();
    }
  }
  BTmodule.write("]}\n");
  
}
