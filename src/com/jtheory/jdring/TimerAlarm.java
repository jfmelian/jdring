/*
 *  com/jtheory/jdring/TimerAlarm.java
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

import java.util.Date;

public class TimerAlarm extends AlarmEntry {
    private int delayMinutes;
    private int periodMinutes;

    /**
     * Creates a new AlarmEntry. Delay format: this alarm will happen once or repeatedly, at increments of the number of minutes given.
     * 
     * @param _name
     *            keeps the alarm unique from other alarms with the same schedule, and used for  Logger.debug(getName(), ging.
     * @param delayMinutes
     *            the alarm delay in minutes (relative to now).
     * @param periodMinutes
     *            the alarm period in minutes. (if <= 0 then the alarm it is not rescheduled when reached)
     * @param _ringInDedicatedThread
     * @param listener
     *            the alarm listener.
     */
    public TimerAlarm(String _name, int delayMinutes, int periodMinutes, boolean _ringInDedicatedThread, AlarmListener _listener) {

        setName(_name);
        setRingInDedicatedThread(_ringInDedicatedThread);
        this.delayMinutes = (delayMinutes >= 0) ? delayMinutes : 0;
        this.periodMinutes = periodMinutes;
        isRepeating = (periodMinutes > 0);
        listener = _listener;

        setAlarmTime(System.currentTimeMillis() + (delayMinutes * 60000));
    }

    protected long nextAlarm() {
        if (!isRepeating)
            return -1;
        long currentTime = System.currentTimeMillis();
        long currentAlarm = (getAlarmTime() > currentTime) ? getAlarmTime() : currentTime ;
        Logger.debug(getName(), "now: " + new Date(currentAlarm));
        
        currentAlarm += (periodMinutes * 60000);
        
        Logger.debug(getName(), "after add period: " + new Date(currentAlarm));
        
        
        return currentAlarm;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public int getPeriodMinutes() {
        return periodMinutes;
    }

    /**
     * @return a string representation of this alarm.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Alarm (uuid=" + getUUID() + " name=" + getName() + ") params");
        sb.append(" delay=");
        sb.append(delayMinutes);
        sb.append(" period=");
        sb.append(periodMinutes);
        sb.append(" is repeating=");
        sb.append(isRepeating);
        sb.append(" (next alarm date=" + new Date(getAlarmTime()) + ")");
        return sb.toString();
    }
}
