/*
 *  com/jtheory/jdring/WeeklyAlarm.java
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

public class WeeklyAlarm extends AlarmEntry {
    private TimeOfDay start;
    private TimeOfDay end;
    private int period;
    private int[] daysOfWeek = { -1 };
    private DailyAlarm dayAlarm;

    public WeeklyAlarm(String name, int[] days, String start, String end, int period, boolean _ringInDedicatedThread, AlarmListener _listener)
            throws InvalidParameterException {
        this(name, days, new TimeOfDay(start), new TimeOfDay(end), period, _ringInDedicatedThread, _listener);
    }

    public WeeklyAlarm(String name, int[] days, TimeOfDay start, TimeOfDay end, int period, boolean _ringInDedicatedThread, AlarmListener listener) throws InvalidParameterException{
        setName(name);
        setRingInDedicatedThread(_ringInDedicatedThread);
        this.start = start;
        this.end = end;
        this.period = period;
        this.listener = listener;

        Arrays.sort(days);
        this.daysOfWeek = AlarmUtils.discardValuesOverMax(days, AlarmUtils.MaxDayOfWeek);
        if (this.daysOfWeek.length == 0) {
            this.daysOfWeek = new int[1];
            this.daysOfWeek[0] = -1;
        }

        dayAlarm = new DailyAlarm(name, start, end, period);
        updateAlarmTime();
    }

    public WeeklyAlarm(String name, String daysPattern, String start, String end, int period, boolean _ringInDedicatedThread, AlarmListener _listener)
            throws InvalidParameterException {
        this(name,  new SchedulingPattern().parseDayOfWeeks(daysPattern), new TimeOfDay(start), new TimeOfDay(end), period, _ringInDedicatedThread,  _listener);
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
    public int[] getDaysOfWeek(){
        return daysOfWeek;
    }
    
    protected long nextAlarm() {
        
        Logger.debug(getName(), "now: " + new Date(getAlarmTime()));

        int currentDayOfWeek;
        
        dayAlarm.setAlarmTime(getAlarmTime());
        dayAlarm.updateAlarmTime();
        long newAlarmTime =  dayAlarm.getAlarmTime();
        Calendar nextAlarm = Calendar.getInstance();
        nextAlarm.setTimeInMillis(newAlarmTime);
        Logger.debug(getName(), "after internal dailyAlarm: " + nextAlarm.getTime());
        
        currentDayOfWeek = nextAlarm.get(Calendar.DAY_OF_WEEK);
        if (!AlarmUtils.isIn(currentDayOfWeek, daysOfWeek)) {
            nextAlarm.add(Calendar.DAY_OF_YEAR, 1); // add one day
            updateDayOfWeek(nextAlarm);
            nextAlarm.set(Calendar.HOUR_OF_DAY, start.getHours());
            nextAlarm.set(Calendar.MINUTE, start.getMinutes());
            nextAlarm.set(Calendar.SECOND, start.getSeconds());
            nextAlarm.set(Calendar.MILLISECOND, start.getMillis());
        }
        Logger.debug(getName(), "after dayOfWeek: " + nextAlarm.getTime());
        return nextAlarm.getTimeInMillis();
    }

    void updateDayOfWeek(Calendar currentAlarm) {
        int currentDayOfWeek = currentAlarm.get(Calendar.DAY_OF_WEEK);
        int offset = 0;

        // loop until we have a valid day AND month (if current is invalid)
        while (!AlarmUtils.isIn(currentDayOfWeek, daysOfWeek)) {
            // advance to the next valid day of week, if necessary
            offset = AlarmUtils.getOffsetToNextOrEqual(currentDayOfWeek, AlarmUtils.MinDayOfWeek, AlarmUtils.MaxDayOfWeek, daysOfWeek);
            currentAlarm.add(Calendar.DAY_OF_YEAR, offset);

            currentDayOfWeek = currentAlarm.get(Calendar.DAY_OF_WEEK);
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
        sb.append(" dayOfWeek=");
        sb.append("{" + AlarmUtils.arrToString(daysOfWeek) + "}");
        sb.append(" is repeating=");
        sb.append(isRepeating);
        sb.append(" (next alarm date=" + new Date(getAlarmTime()) + ")");
        return sb.toString();
    }

}
