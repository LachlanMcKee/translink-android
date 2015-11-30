package com.lach.common.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static boolean constrainDate(Calendar inputCalendar, Date minimumDate, int numberOfDays) {
        Calendar minimumCalendar = Calendar.getInstance();
        minimumCalendar.setTime(minimumDate);

        Calendar maximumCalendar = Calendar.getInstance();
        maximumCalendar.setTime(minimumDate);
        maximumCalendar.add(Calendar.DAY_OF_MONTH, numberOfDays);

        if (minimumCalendar.after(inputCalendar)) {
            inputCalendar.set(Calendar.YEAR, minimumCalendar.get(Calendar.YEAR));
            inputCalendar.set(Calendar.MONTH, minimumCalendar.get(Calendar.MONTH));
            inputCalendar.set(Calendar.DAY_OF_MONTH, minimumCalendar.get(Calendar.DAY_OF_MONTH));

            return true;

        } else if (maximumCalendar.before(inputCalendar)) {
            inputCalendar.set(Calendar.YEAR, maximumCalendar.get(Calendar.YEAR));
            inputCalendar.set(Calendar.MONTH, maximumCalendar.get(Calendar.MONTH));
            inputCalendar.set(Calendar.DAY_OF_MONTH, maximumCalendar.get(Calendar.DAY_OF_MONTH));

            return true;
        }

        return false;
    }
}
