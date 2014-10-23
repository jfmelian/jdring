/*
 *  com/jtheory/jdring/DailyAlarm.java
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

import java.util.Calendar;
import java.util.Date;

/**
 * @author jmelian
 * 
 */
public class DailyAlarm extends AlarmEntry {

    private TimeOfDay start;
    private TimeOfDay end;
    int period; // in minutes

    public DailyAlarm(String _name, String _start, String _end, int _period) throws InvalidParameterException {
        this(_name, new TimeOfDay(_start), new TimeOfDay(_end), _period, false, null);
    }

    public DailyAlarm(String _name, TimeOfDay _start, TimeOfDay _end, int _period) throws InvalidParameterException {
        this(_name, _start, _end, _period, false, null);
    }

    public DailyAlarm(String name, String start, String end, int period, boolean _ringInDedicatedThread, AlarmListener _listener)
            throws InvalidParameterException{
        this(name, new TimeOfDay(start), new TimeOfDay(end), period, _ringInDedicatedThread, _listener);
    }

    public DailyAlarm(String name, TimeOfDay start, TimeOfDay end, int period, boolean _ringInDedicatedThread, AlarmListener _listener) throws InvalidParameterException {
        setName(name);
        setRingInDedicatedThread(_ringInDedicatedThread);
        this.start = start;
        this.end = end;
        this.period = period;
        this.listener = _listener;
        isRepeating = (period > 0);
        
        if (start.compareTo(end) > 0 )
            throw new InvalidParameterException("End must greater then Start");
        updateAlarmTime();
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
    
    protected long nextAlarm() {
        if (!isRepeating)
            return -1;

        Calendar nextAlarm = Calendar.getInstance();
        
        
        if (getAlarmTime() < nextAlarm.getTimeInMillis()) {
             Logger.debug(getName(), "now: " + nextAlarm.getTime());
            nextAlarm.set(Calendar.HOUR_OF_DAY, start.getHours());
            nextAlarm.set(Calendar.MINUTE, start.getMinutes());
            nextAlarm.set(Calendar.SECOND, start.getSeconds());
            nextAlarm.set(Calendar.MILLISECOND, start.getMillis());
        } else {
            nextAlarm.setTimeInMillis(getAlarmTime());
             Logger.debug(getName(), "now: " + nextAlarm.getTime());
            nextAlarm.add(Calendar.MINUTE, period);
        }

        // add period until < now + 2s
        checkDateAlarm(nextAlarm, period);
         Logger.debug(getName(), "after checkDateAlarm: " + nextAlarm.getTime());

        // not before start
        while (start.compareTo(nextAlarm) > 0) {
            nextAlarm.add(Calendar.MINUTE, period);
        }
         Logger.debug(getName(), "after not before start: " + nextAlarm.getTime());

        // not after start
        if (end.compareTo(nextAlarm) < 0) {
            nextAlarm.add(Calendar.DAY_OF_YEAR, 1); // add one day
            nextAlarm.set(Calendar.HOUR_OF_DAY, start.getHours());
            nextAlarm.set(Calendar.MINUTE, start.getMinutes());
            nextAlarm.set(Calendar.SECOND, start.getSeconds());
            nextAlarm.set(Calendar.MILLISECOND, start.getMillis());
        }
         Logger.debug(getName(), "after not after end: " + nextAlarm.getTime());

        return nextAlarm.getTimeInMillis();
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
        sb.append(" is repeating=");
        sb.append(isRepeating);
        sb.append(" (next alarm date=" + new Date(getAlarmTime()) + ")");
        return sb.toString();
    }

}
