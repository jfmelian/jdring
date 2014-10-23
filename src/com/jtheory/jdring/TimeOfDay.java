/*
 *  com/jtheory/jdring/TimeOfDay.java
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

public class TimeOfDay implements Comparable, java.io.Serializable {

    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int millis;

    public TimeOfDay(Calendar cal) {
        hours = cal.get(Calendar.HOUR_OF_DAY);
        minutes = cal.get(Calendar.MINUTE);
        seconds = cal.get(Calendar.SECOND);
        millis = cal.get(Calendar.MILLISECOND);
    }

    public TimeOfDay(int hour, int min) {
        this.hours = hour;
        this.minutes = min;
        this.seconds = 0;
        this.millis = 0;
    }

    public TimeOfDay(int hour, int min, int sec) {
        this.hours = hour;
        this.minutes = min;
        this.seconds = sec;
        this.millis = 0;
    }

    public TimeOfDay(int hour, int min, int sec, int millis) {
        this.hours = hour;
        this.minutes = min;
        this.seconds = sec;
        this.millis = millis;
    }

    public TimeOfDay(String s) throws InvalidParameterException {
        int firstColon;
        int secondColon;
        int idx;

        if ((s == null) || s.isEmpty())
            throw new InvalidParameterException("Empty string");

        firstColon = s.indexOf(':');
        secondColon = s.indexOf(':', firstColon + 1);
        idx = s.indexOf('.', secondColon + 1);

        if ((firstColon > 0) & (secondColon > 0) & (idx > 0) & (idx < s.length() - 1)) {
            hours = Integer.parseInt(s.substring(0, firstColon));
            minutes = Integer.parseInt(s.substring(firstColon + 1, secondColon));
            seconds = Integer.parseInt(s.substring(secondColon + 1, idx));
            millis = Integer.parseInt(s.substring(idx + 1));
        } else if ((firstColon > 0) & (secondColon > 0) & (secondColon < s.length() - 1)) {
            hours = Integer.parseInt(s.substring(0, firstColon));
            minutes = Integer.parseInt(s.substring(firstColon + 1, secondColon));
            seconds = Integer.parseInt(s.substring(secondColon + 1));
            millis = 0;
        } else if (firstColon > 0) {
            hours = Integer.parseInt(s.substring(0, firstColon));
            minutes = Integer.parseInt(s.substring(firstColon + 1));
            seconds = 0;
            millis = 0;
        } else {
            throw new InvalidParameterException("Bad TimeOfDay pattern : " + s);
        }
    }

    int getHours() {
        return hours;
    }

    /**
     * @return the minutes
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @return the seconds
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * @return the millis
     */
    public int getMillis() {
        return millis;
    }
    public int compareTo(Object that) {
        if (that instanceof TimeOfDay)
            return compareTo((TimeOfDay) that);
        else
            return this.getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
    }

    public int compareTo(TimeOfDay arg0) {
        TimeOfDay other = arg0;
        if (hours < other.hours)
            return -1;
        else if (hours > other.hours)
            return 1;
        else if (minutes < other.minutes)
            return -1;
        else if (minutes > other.minutes)
            return 1;
        else if (seconds < other.seconds)
            return -1;
        else if (seconds > other.seconds)
            return 1;
        else if (millis < other.millis)
            return -1;
        else if (millis > other.millis)
            return 1;
        else
            return 0;
    }

    public int compareTo(Calendar arg0) {
        TimeOfDay other = new TimeOfDay(arg0);
        return compareTo(other);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = result * prime + hours;
        result = result * prime + minutes;
        result = result * prime + seconds;
        result = result * prime + millis;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeOfDay other = (TimeOfDay) obj;
        if (hours != other.hours)
            return false;
        if (minutes != other.minutes)
            return false;
        if (seconds != other.seconds)
            return false;
        if (millis != other.millis)
            return false;
        return true;
    }

    /**
     * @return a string representation of this alarm.
     */
    public String toString() {
        String hourString;
        String minuteString;
        String secondString;
        String milliString;

        if (hours < 10)
            hourString = "0" + hours;
        else
            hourString = Integer.toString(hours);

        if (minutes < 10)
            minuteString = "0" + minutes;
        else
            minuteString = Integer.toString(minutes);

        if (seconds < 10)
            secondString = "0" + seconds;
        else
            secondString = Integer.toString(seconds);

        if (millis < 10)
            milliString = "00" + millis;
        else if (millis < 100)
            milliString = "0" + millis;
        else
            milliString = Integer.toString(millis);

        return (hourString + ":" + minuteString + ":" + secondString + '.' + milliString);
    }

}
