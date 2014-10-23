/*
 *  com/jtheory/jdring/MonthlyAlarm.java
 *  Copyright (C) 1999 - 2004 jtheory creations, Olivier Dedieu et al.
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package com.jtheory.jdring;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class MonthlyAlarm extends AlarmEntry {

    private TimeOfDay start;
    private TimeOfDay end;
    private int period;
    private DailyAlarm dayAlarm;
    private int[] daysOfMonth = { -1 };

    public MonthlyAlarm(String name, int[] days, String start, String end, int period, boolean _ringInDedicatedThread, AlarmListener listener)
            throws InvalidParameterException {
        this(name, days, new TimeOfDay(start), new TimeOfDay(end), period, _ringInDedicatedThread, listener);
    }

    public MonthlyAlarm(String name, int[] days, TimeOfDay start, TimeOfDay end, int period, boolean _ringInDedicatedThread, AlarmListener listener) throws InvalidParameterException{
        setName(name);
        setRingInDedicatedThread(_ringInDedicatedThread);
        this.start = start;
        this.end = end;
        this.period = period;
        this.listener = listener;

        Arrays.sort(days);
        this.daysOfMonth = AlarmUtils.discardValuesOverMax(days, AlarmUtils.LAST_DAY_OF_MONTH);
        if (this.daysOfMonth.length == 0) {
            this.daysOfMonth = new int[1];
            this.daysOfMonth[0] = -1;
        }

        dayAlarm = new DailyAlarm(name, start, end, period);
        updateAlarmTime();
    }

    public MonthlyAlarm(String name, String daysPattern, String start, String end, int period, boolean _ringInDedicatedThread, AlarmListener listener)
            throws InvalidParameterException {
        this(name,  new SchedulingPattern().parseDayOfMonths(daysPattern), new TimeOfDay(start), new TimeOfDay(end), period, _ringInDedicatedThread,  listener);
    }

    public String getStart(){
        return start.toString();
    }
    public String getEnd(){
        return end.toString();
    }
    public int getPeriod(){
        return period;
    }
    public int[] getDaysOfMonth(){
        return daysOfMonth;
    }
    
    protected long nextAlarm() {
         Logger.debug(getName(), "now: " + new Date(getAlarmTime()));

        int currentDayOfMonth;
        int maxDayOfMonth;
        
        dayAlarm.setAlarmTime(getAlarmTime());
        dayAlarm.updateAlarmTime();
        long newAlarmTime =  dayAlarm.getAlarmTime();
        Calendar nextAlarm = Calendar.getInstance();
        nextAlarm.setTimeInMillis(newAlarmTime);
         Logger.debug(getName(), "after internal dailyAlarm: " + nextAlarm.getTime());
        
        currentDayOfMonth = nextAlarm.get(Calendar.DAY_OF_MONTH);
        maxDayOfMonth = nextAlarm.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (!AlarmUtils.isIn(currentDayOfMonth, daysOfMonth, maxDayOfMonth)) {
            nextAlarm.add(Calendar.DAY_OF_YEAR, 1); // add one day
            updateDayOfMonth(nextAlarm);
            nextAlarm.set(Calendar.HOUR_OF_DAY, start.getHours());
            nextAlarm.set(Calendar.MINUTE, start.getMinutes());
            nextAlarm.set(Calendar.SECOND, start.getSeconds());
            nextAlarm.set(Calendar.MILLISECOND, start.getMillis());
        }
         Logger.debug(getName(), "after dayOfMonth: " + nextAlarm.getTime());
        return nextAlarm.getTimeInMillis();
    }
    void updateDayOfMonth(Calendar alarm) {
        int currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH);
        int maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentMaxDayOfMonth = maxDayOfMonth;
        int offset = 0;

        int[] localDaysOfMonth = AlarmUtils.getDaysOfMonth(daysOfMonth, maxDayOfMonth);

        // loop until we have a valid day AND month (if current is invalid)
        while (!AlarmUtils.isIn(currentDayOfMonth, localDaysOfMonth)) {
            // advance to the next valid day of month, if necessary
            offset = AlarmUtils.getOffsetToNextOrEqual(currentDayOfMonth, AlarmUtils.MinDayOfMonth, maxDayOfMonth, localDaysOfMonth);
            alarm.add(Calendar.DAY_OF_MONTH, offset);

            currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH);
            maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (currentMaxDayOfMonth != maxDayOfMonth) {
                currentMaxDayOfMonth = maxDayOfMonth;
                localDaysOfMonth = AlarmUtils.getDaysOfMonth(daysOfMonth, maxDayOfMonth);
            }
        }
    }

    /**
     * @return a string representation of this alarm.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Alarm (uuid=" + getUUID() + " name=" + getName() + ") params");
        sb.append(" start=");
        sb.append(start);
        sb.append(" end=");
        sb.append(end);
        sb.append(" period=");
        sb.append(period);
        sb.append(" dayOfMonth=");
        sb.append("{" + AlarmUtils.arrToString(daysOfMonth) + "}");
        sb.append(" is repeating=");
        sb.append(isRepeating);
        sb.append(" (next alarm date=" + new Date(getAlarmTime()) + ")");
        return sb.toString();
    }

}
