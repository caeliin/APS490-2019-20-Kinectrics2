#include "Bluetooth.h"
#include "SDcard.h"
#include "RTC.h"

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
