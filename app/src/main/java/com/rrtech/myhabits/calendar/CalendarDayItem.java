package com.rrtech.myhabits.calendar;

import java.time.LocalDate;

public class CalendarDayItem {
    public LocalDate date;
    public boolean isCurrentMonth;
    public boolean isChecked;
    public boolean isMissed;

    public CalendarDayItem(LocalDate date, boolean isCurrentMonth, boolean isChecked, boolean isMissed) {
        this.date = date;
        this.isCurrentMonth = isCurrentMonth;
        this.isChecked = isChecked;
        this.isMissed = isMissed;
    }
}
