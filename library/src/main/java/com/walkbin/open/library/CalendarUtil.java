package com.walkbin.open.library;

import java.util.Calendar;

/**
 * Created by wangxiaxin on 2016/9/19.
 *
 * 日历相关的工具方法
 */
public class CalendarUtil {
    
    /**
     * 获取某个月有多少天
     *
     * @param month
     *            {@link Calendar#JANUARY} ~ {@link Calendar#DECEMBER}
     * @param year
     *            such as 2016
     */
    public static int daysOfMonth(int year,int month) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) ? 29
                        : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }
}
