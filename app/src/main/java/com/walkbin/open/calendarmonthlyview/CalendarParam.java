package com.walkbin.open.calendarmonthlyview;

import java.util.Calendar;

/**
 * Created by richie.wang on 2017/5/6.
 */

public class CalendarParam {
    int year;
    int month;
    int day;

    CalendarParam(int year, int month) {
        this.year = year;
        this.month = month;
    }

    CalendarParam(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    CalendarParam nextMonth() {
        if (month == Calendar.DECEMBER) {
            return new CalendarParam(year + 1, Calendar.JANUARY);
        }
        else {
            return new CalendarParam(year, month + 1);
        }
    }
}