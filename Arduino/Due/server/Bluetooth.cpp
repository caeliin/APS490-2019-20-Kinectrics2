#include "Bluetooth.h"
#include "SDcard.h"
#include "RTC.h"

/*
 * Handle messages over Bluetooth - parse the request and craft the response
 */
void processBT() {
  DynamicJsonDocument received(128);
  DeserializationError err = deserializeJson(received, Serial2);
  if (err) {
    return;
  }

  //debugging purposes - bit errors grr
  if (!received.containsKey("request")) {
    String output;
    serializeJson(received, output);
    Serial.println(output);
  }
  
  const char *req = received["request"];
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
    Serial2.write("{\"response\": \"EXIT\"}\n");
  }
  else {
    Serial2.write("{\"response\": \"ERROR\", \"error\": \"");
    Serial2.write(req);
    Serial2.write("\"is not a valid request\"}\n");
  }
  return;
}

/*
 * Handle a dump request
 * Dump the SD card's files and send over BT
 */
void dump() {
  int currentYear = rtc.getTime().year;
  char fileName[9];
  
  Serial2.write("{\"response\": \"DATA\", \"data\" : [");
  boolean isFirstFileRead = true;
  
  for (int file_year = 2000; file_year <= currentYear; file_year++) {
    sprintf(fileName, "%4d.txt", file_year);

    if (SD.exists(fileName)) {
      //Send the contents of the file over BT
      serverFile = SD.open(fileName); 
      
      if (isFirstFileRead) {
        isFirstFileRead = false;
      }
      else {
        Serial2.write(", ");
      }
      
      bool first = true; //lists are a pain
      char line[] = "([YYYY, MM, DD, HH, MM, SS], V.VV)";
      while (serverFile.available()) {
        //write the comma that separates objects
        if (!first) {
          Serial2.write(", ");
        }
        else {
          first = false;
        }
        
        //write the line for a single entry
        fillFileLine(line);
        Serial.println(line);
        Serial2.write(line); 
      }
      serverFile.close();
    }
  }
  Serial2.write("]}\n");
}

/*
 * Handle a query request
 * Find the first entry that is more recent than the requested date and send them over BT
 */
void query(int year, int month, int day, int hour, int minute, int second) {

  int currentYear = rtc.getTime().year;
  char fileName[9];
  
  Serial2.write("{\"response\": \"DATA\", \"data\" : [");
  boolean isFirstFileRead = true;
  
  for (int file_year = year; file_year <= currentYear; file_year++) {
    sprintf(fileName, "%4d.txt", file_year);
    if (SD.exists(fileName)) {
      //Send the contents of the file over BT
      serverFile = SD.open(fileName); 

      //write the comma between entries between files
      if (isFirstFileRead) {
        isFirstFileRead = false;
      }
      else {
        Serial2.write(", ");
      }
      
      bool first = true; //lists are a pain
      char line[] = "([YYYY, MM, DD, HH, MM, SS], V.VV)";

      if (file_year == year) {
        seekInFile(month, day, hour, minute, second); //move the cursor to the right position in the file
      }
      
      while (serverFile.available()) {
        //write the comma that separates objects
        if (!first) {
          Serial2.write(", ");
        }
        else {
          first = false;
        }
        
        //write the line for a single entry
        fillFileLine(line);
        Serial.println(line);
        Serial2.write(line); 
      }
      serverFile.close();
    }
  }
  Serial2.write("]}\n"); 
}
