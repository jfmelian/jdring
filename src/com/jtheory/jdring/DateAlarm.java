/*
 *  com/jtheory/jdring/DateAlarm.java
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

public class DateAlarm extends AlarmEntry {

    /**
     * Creates a new AlarmEntry. Fixed date format: this alarm will happen once, at the timestamp given.
     * 
     * @param _ringInDedicatedThread
     * 
     * @param date
     *            the alarm date to be added.
     * @param listener
     *            the alarm listener.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second away from the current date).
     */
    public DateAlarm(String _name, Date _date, boolean _ringInDedicatedThread, AlarmListener _listener) throws PastDateException {

        setName(_name);
        setRingInDedicatedThread(_ringInDedicatedThread);
        listener = _listener;
        Calendar alarm = Calendar.getInstance();
        alarm.setTime(_date);

        isRepeating = false;
        setAlarmTime(_date.getTime());
        checkAlarmTime();
    }

    protected long nextAlarm() {
        return -1;
    }

    /**
     * @return a string representation of this alarm.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Alarm (uuid=" + getUUID() + " name=" + getName() + ") params");
        sb.append(" alarm date=");
        sb.append(new Date(getAlarmTime()));
        return sb.toString();
    }

}
