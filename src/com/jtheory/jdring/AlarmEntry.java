/*
 *  com/jtheory/jdring/AlarmEntry.java
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
 */
package com.jtheory.jdring;

import java.util.Calendar;
import java.util.UUID;

public abstract class AlarmEntry implements Comparable, java.io.Serializable {

    private String name = "";
    private UUID uuid = UUID.randomUUID();
    private boolean ringInDedicatedThread = false;
    private long alarmTime=0;
    protected long lastUpdateTime = 0;
    protected boolean isRepeating = true;

    protected transient AlarmListener listener = null;

    /**
     * Checks that alarm is not in the past, or less than 1 second away.
     * 
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second in the future).
     */
    public void checkAlarmTime() throws PastDateException {
        long delay = alarmTime - System.currentTimeMillis();

        if (delay <= 1000) {
            throw new PastDateException();
        }
    }
    /**
     * Get the listener
     */
    public AlarmListener getAlarmListener(){
        return listener;
    }

    /**
     * Compares this AlarmEntry with the specified AlarmEntry for order. One twist -- if the alarmTime matches, this alarm will STILL place itself before the
     * other based on the lastUpdateTime. If the other alarm has been rung more recently, this one should get priority.
     * 
     * @param obj
     *            the AlarmEntry with which to compare.
     * @return a negative integer, zero, or a positive integer as this AlarmEntry is less than, equal to, or greater than the given AlarmEntry.
     * @exception ClassCastException
     *                if the specified Object's type prevents it from being compared to this AlarmEntry.
     */
    public final int compareTo(Object that) {
        if (that instanceof AlarmEntry)
            return compareTo((AlarmEntry) that);
        else
            return this.getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
    }
    public final int compareTo(AlarmEntry other) {
        int uuidCcompareTo;

        if (alarmTime < other.alarmTime)
            return -1;
        else if (alarmTime > other.alarmTime)
            return 1;
        else if (lastUpdateTime < other.lastUpdateTime)
            return -1;
        else if (lastUpdateTime > other.lastUpdateTime)
            return 1;
        else if ((uuidCcompareTo = uuid.compareTo(other.uuid)) != 0)
            return uuidCcompareTo;
        else
            return name.compareTo(other.name);
    }

    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj == null)
            return false;
        else if (getClass() != obj.getClass())
            return false;

        AlarmEntry other = (AlarmEntry) obj;
        if (alarmTime != other.alarmTime)
            return false;
        else if (lastUpdateTime != other.lastUpdateTime)
            return false;
        else if (uuid.equals(other.uuid))
            return false;
        else if (!name.equals(other.name))
            return false;
        return true;
    }

    /**
     * @return the ringInNewThread
     */
    public long getAlarmTime() {
        return alarmTime;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the uuid
     */
    public String getUUID() {
        return uuid.toString();
    }

    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (alarmTime ^ (alarmTime >>> 32));
        result = prime * result + (int) (lastUpdateTime ^ (lastUpdateTime >>> 32));
        result = prime * result + uuid.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    /**
     * By default, the AlarmListeners for all alarms will be notified in a dedicatedthe same thread (so a long-running handleAlarm() implementation will cause
     * other alarms to wait until it completes). Call this method to notify the listener to this alarm in a new Thread, so other alarms won't be delayed.
     */
    public boolean isRingInDedicatedThread() {
        return ringInDedicatedThread;
    }

    /**
     * Calcaulte next alarm
     */
    protected abstract long nextAlarm();

    /**
     * Notifies the listener.
     */
    protected void ringAlarm() {
        if (listener != null)
            listener.handleAlarm(this);
    }

    /**
     * @param ringInDedicatedThread
     *            the ringInNewThread to set
     */
    protected void setAlarmTime(long alarmTime) {
        this.alarmTime = alarmTime;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
        if (this.name == null)
            this.name = uuid.toString();
    }

    /**
     * @param ringInNewThread
     *            the ringInNewThread to set
     */
    public void setRingInDedicatedThread(boolean ringInDedicatedThread) {
        this.ringInDedicatedThread = ringInDedicatedThread;
    }

    /**
     * Updates this alarm entry to the next valid alarm time, AFTER the current time.
     */
    protected boolean updateAlarmTime() {
        Logger.debug(name, "updateAlarmTime()");
        boolean ret = false;

        if (isRepeating) {
            long nextAlarm = nextAlarm();
            if (nextAlarm > 0) {
                setAlarmTime(nextAlarm);
                ret = true;
            }
        }

        Logger.debug(name, "updateAlarmTime() return " + ret);
        return ret;
    }

    /**
     * check the next alarm : must be > time + 2 s
     * @param newAlarm
     * @param periodMinute
     */
    protected void checkDateAlarm(Calendar newAlarm, int periodMinute){
        
        Calendar now = Calendar.getInstance();
        now.set(Calendar.SECOND, 2); 
        if (newAlarm.compareTo(now) < 0){
            // set the day
            if (newAlarm.get(Calendar.YEAR) < now.get(Calendar.YEAR))
                newAlarm.set(Calendar.YEAR, now.get(Calendar.YEAR));
            if (newAlarm.get(Calendar.DAY_OF_YEAR) < now.get(Calendar.DAY_OF_YEAR))
                newAlarm.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
            // set a time <= now
            int delta = (int)((now.getTimeInMillis() - newAlarm.getTimeInMillis()) / 60000); // delta (24*60*60*1000) < Integer.MAX_VALUE
            int nbPeriod = delta / periodMinute;
            if (nbPeriod > 0 )
                newAlarm.add(Calendar.MINUTE, nbPeriod * periodMinute);
        }
        
        while (newAlarm.compareTo(now) < 0){
            newAlarm.add(Calendar.MINUTE, periodMinute);
        }
    }


}
