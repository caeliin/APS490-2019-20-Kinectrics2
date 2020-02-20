#ifndef RTC_H
#define RTC_H

/* RTC stuff */
#include "DS3231.h"
extern DS3231 rtc; //initialize RTC
extern Time nextRun;

void printTime(Time time);
Time getTimeToRunNext(int year_d, int month_d, int day_d, int hour_d, int minute_d, int second_d);
bool isRunTime(Time target);

#endif
