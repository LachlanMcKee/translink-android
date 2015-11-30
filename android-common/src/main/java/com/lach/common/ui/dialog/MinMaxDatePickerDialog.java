package com.lach.common.ui.dialog;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.widget.DatePicker;

import com.lach.common.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class MinMaxDatePickerDialog extends DatePickerDialog {

    private final ConstraintParams constraints;

    public MinMaxDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, ConstraintParams constraints) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        this.constraints = constraints;

        // Gingerbread doesn't support assigning a min/max.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            DatePicker datePicker = getDatePicker();
            datePicker.setMinDate(constraints.minimumCalendar.getTimeInMillis());
            datePicker.setMaxDate(constraints.maximumCalendar.getTimeInMillis());
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        // Gingerbread doesn't support assigning a min/max.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);

            DateUtil.constrainDate(newDate, constraints.minimumCalendar.getTime(), constraints.numberOfDays);

            year = newDate.get(Calendar.YEAR);
            month = newDate.get(Calendar.MONTH);
            day = newDate.get(Calendar.DAY_OF_MONTH);
        }
        super.onDateChanged(view, year, month, day);
    }

    public static class ConstraintParams {
        final Calendar minimumCalendar;
        final Calendar maximumCalendar;
        final int numberOfDays;

        @SuppressWarnings("SameParameterValue")
        public ConstraintParams(Date date, int numberOfDays) {
            this.numberOfDays = numberOfDays;

            minimumCalendar = Calendar.getInstance();
            minimumCalendar.setTime(date);

            maximumCalendar = Calendar.getInstance();
            maximumCalendar.setTime(date);
            maximumCalendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
        }
    }

}
