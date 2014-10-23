/*
 *  com/jtheory/jdring/AlarmEntry.java
 *  Copyright (C) 1999 - 2004 jtheory creations, Olivier Dedieu et al.
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Library General Public License as published
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
 * This class represents the attributes of an alarm.
 * 
 * @author Rob Whelan, Olivier Dedieu, David Sims, Simon Bécot, Jim Lerner
 * @version 1.4.1, 2004/04/02
 */
public class CronAlarm extends AlarmEntry {

    private CronParameter cronEntry = new CronParameter();
    private static int minMinute = 0;
    private static int maxMinute = 59;

    public CronAlarm(String _name, String pattern, boolean dedicatedThread, AlarmListener _listener) throws InvalidParameterException {
        
        this(_name, new SchedulingPattern().parseCron(pattern), dedicatedThread, _listener);
    }

    /**
     * <p>
     * Creates a new AlarmEntry. Basic cron format - use each field to restrict alarms to a specific minute, hour, etc. OR pass in -1 to allow all values of
     * that field.
     * </p>
     * 
     * <p>
     * Params of (30, 13, -1, -1, 2, -1, listener) schedule an alarm for 1:30pm every Monday.
     * </p>
     * 
     * <p>
     * NOTE: if both dayOfMonth and dayOfWeek are restricted, each alarm will be scheduled for the sooner match.
     * </p>
     * 
     * @param minute
     *            minute of the alarm. Allowed values 0-59.
     * @param hour
     *            hour of the alarm. Allowed values 0-23.
     * @param dayOfMonth
     *            day of month of the alarm (-1 if every day). Allowed values 1-31.
     * @param month
     *            month of the alarm (-1 if every month). Allowed values 0-11 (0 = January, 1 = February, ...). <code>java.util.Calendar</code> constants can be
     *            used.
     * @param dayOfWeek
     *            day of week of the alarm (-1 if every day). This attribute is exclusive with <code>dayOfMonth</code>. Allowed values 1-7 (1 = Sunday, 2 =
     *            Monday, ...). <code>java.util.Calendar</code> constants can be used.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @throws PastDateException
     */
    public CronAlarm(String _name, int _minute, int _hour, int _dayOfMonth, int _month, int _dayOfWeek, boolean dedicatedThread, AlarmListener _listener) {
        this(_name, new int[] { _minute }, new int[] { _hour }, new int[] { _dayOfMonth }, new int[] { _month }, new int[] { _dayOfWeek }, dedicatedThread,
                _listener);
    }

    /**
     * <p>
     * Creates a new AlarmEntry. Extended cron format - supports lists of values for each field, or {-1} to allow all values for that field.
     * </p>
     * 
     * <p>
     * Params of (30, 13, -1, -1, 2, -1, listener) schedule an alarm for 1:30pm every Monday.
     * </p>
     * 
     * <p>
     * NOTE: if both dayOfMonth and dayOfWeek are restricted, each alarm will be scheduled for the sooner match.
     * </p>
     * 
     * @param minutes
     *            valid minutes of the alarm. Allowed values 0-59, or {-1} for all.
     * @param cronEntry
     *            .getHours() valid cronEntry.getHours() of the alarm. Allowed values 0-23, or {-1} for all.
     * @param daysOfMonth
     *            valid days of month of the alarm. Allowed values 1-31, or {-1} for all.
     * @param months
     *            valid months of the alarm. Allowed values 0-11 (0 = January, 1 = February, ...), or {-1} for all. <code>java.util.Calendar</code> constants
     *            can be used.
     * @param daysOfWeek
     *            valid days of week of the alarm. This attribute is exclusive with <code>dayOfMonth</code>. Allowed values 1-7 (1 = Sunday, 2 = Monday, ...),
     *            or {-1} for all. <code>java.util.Calendar</code> constants can be used.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @throws PastDateException
     */
    public CronAlarm(String _name, int[] _minutes, int[] _hours, int[] _daysOfMonth, int[] _months, int[] _daysOfWeek, boolean dedicatedThread,
            AlarmListener _listener) {
        this(_name, new CronParameter(_minutes, _hours, _daysOfMonth, _months, _daysOfWeek), dedicatedThread, _listener);
    }


    public CronAlarm(String _name, CronParameter _cronEntry, boolean dedicatedThread, AlarmListener _listener) {

        setName(_name);
        setRingInDedicatedThread(dedicatedThread);
        cronEntry.setMinutes(_cronEntry.getMinutes());
        cronEntry.setHours(_cronEntry.getHours());
        cronEntry.setDaysOfMonth(_cronEntry.getDaysOfMonth());
        cronEntry.setMonths(_cronEntry.getMonths());
        cronEntry.setDaysOfWeek(_cronEntry.getDaysOfWeek());
        listener = _listener;

        updateAlarmTime();
    }

    /**
     * Updates this alarm entry to the next valid alarm time, AFTER the current time.
     */
    protected long nextAlarm() {

        Calendar nextAlarm = Calendar.getInstance();

        if (getAlarmTime() > nextAlarm.getTimeInMillis()) {
            nextAlarm.setTimeInMillis(getAlarmTime());
            Logger.debug(getName(), "now: " + nextAlarm.getTime());
            nextAlarm.add(Calendar.MINUTE, 1); // next minute
        }else
             Logger.debug(getName(), "now: " + nextAlarm.getTime());
        nextAlarm.set(Calendar.SECOND, 0);
        nextAlarm.set(Calendar.MILLISECOND, 0);
        // nextAlarm may decrease by 59.999 seconds
        // add period until < now + 2s
        checkDateAlarm(nextAlarm, 1);
         Logger.debug(getName(), "after checkDateAlarm: " + nextAlarm.getTime());

        //
        // the updates work in a cascade -- if next minute value is in the
        // following hour, hour is incremented. If next valid hour value is
        // in the following day, day is incremented, and so on.
        //

        // increase alarm minutes
        int current = nextAlarm.get(Calendar.MINUTE);
        int offset = 0;
        /*
         * // force increment at least to next minute offset = AlarmUtils.getOffsetToNext(current, minMinute, maxMinute, cronEntry.getMinutes());
         */
        // update minute if necessary
        offset = AlarmUtils.getOffsetToNextOrEqual(current, minMinute, maxMinute, cronEntry.getMinutes());

        nextAlarm.add(Calendar.MINUTE, offset);
         Logger.debug(getName(), "after min: " + nextAlarm.getTime());

        // update alarm cronEntry.getHours() if necessary
        current = nextAlarm.get(Calendar.HOUR_OF_DAY); // (as updated by minute shift)
        offset = AlarmUtils.getOffsetToNextOrEqual(current, AlarmUtils.MinHour, AlarmUtils.MaxHour, cronEntry.getHours());
        nextAlarm.add(Calendar.HOUR_OF_DAY, offset);
         Logger.debug(getName(), "after hour (current:" + current + "): " + nextAlarm.getTime());

        //
        // If days of month AND days of week are restricted, we take whichever match
        // comes sooner.
        // If only one is restricted, take the first match for that one.
        // If neither is restricted, don't do anything.
        //
        if (cronEntry.getDaysOfMonth()[0] != -1 && cronEntry.getDaysOfWeek()[0] != -1) {
            
            // BOTH are restricted - take earlier match
            Calendar dayOfWeekAlarm = (Calendar)nextAlarm.clone();
            updateDayOfWeekAndMonth( dayOfWeekAlarm );
            
            Calendar dayOfMonthAlarm = (Calendar)nextAlarm.clone();
            updateDayOfMonthAndMonth( dayOfMonthAlarm );
            
            // take the earlier one
            if( dayOfMonthAlarm.getTime().getTime() < dayOfWeekAlarm.getTime().getTime() )
            {
                nextAlarm = dayOfMonthAlarm;
                 Logger.debug(getName(),  "after dayOfMonth CLOSER: " + nextAlarm.getTime() );
            }
            else
            {
                nextAlarm = dayOfWeekAlarm;
                 Logger.debug(getName(),  "after dayOfWeek CLOSER: " + nextAlarm.getTime() );
            }

        } else if (cronEntry.getDaysOfWeek()[0] != -1) // only dayOfWeek is restricted
        {
            // update dayInWeek and month if necessary
            updateDayOfWeekAndMonth(nextAlarm);
             Logger.debug(getName(), "after dayOfWeek: " + nextAlarm.getTime());
        } else if (cronEntry.getDaysOfMonth()[0] != -1) // only dayOfMonth is restricted
        {
            // update dayInMonth and month if necessary
            updateDayOfMonthAndMonth(nextAlarm);
             Logger.debug(getName(), "after dayOfMonth: " + nextAlarm.getTime());
        }
        // else if neither is restricted (both[0] == -1), we don't need to do anything.

         Logger.debug(getName(), "alarm: " + nextAlarm.getTime());

        return nextAlarm.getTimeInMillis();
    }

    /**
     * daysInMonth can't use simple offsets like the other fields, because the number of days varies per month (think of an alarm that executes on every 31st).
     * Instead we advance month and dayInMonth together until we're on a matching value pair.
     */
    void updateDayOfMonthAndMonth(Calendar alarm) {
        int currentMonth = alarm.get(Calendar.MONTH);
        int currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH);
        int maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentMaxDayOfMonth = maxDayOfMonth;
        int[] localDaysOfMonth = AlarmUtils.getDaysOfMonth(cronEntry.getDaysOfMonth(), maxDayOfMonth);
        int offset = 0;

        // loop until we have a valid day AND month (if current is invalid)
        while (!AlarmUtils.isIn(currentMonth, cronEntry.getMonths()) || !AlarmUtils.isIn(currentDayOfMonth, localDaysOfMonth, maxDayOfMonth)) {
            // if current month is invalid, advance to 1st day of next valid month
            if (!AlarmUtils.isIn(currentMonth, cronEntry.getMonths())) {
                offset = AlarmUtils.getOffsetToNextOrEqual(currentMonth, AlarmUtils.MinMonth, AlarmUtils.MaxMonth, cronEntry.getMonths());
                alarm.add(Calendar.MONTH, offset);
                alarm.set(Calendar.DAY_OF_MONTH, 1);
                currentDayOfMonth = 1;
            }

            maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (currentMaxDayOfMonth != maxDayOfMonth) {
                currentMaxDayOfMonth = maxDayOfMonth;
                localDaysOfMonth = AlarmUtils.getDaysOfMonth(cronEntry.getDaysOfMonth(), maxDayOfMonth);
            }
            // advance to the next valid day of month, if necessary
            if (!AlarmUtils.isIn(currentDayOfMonth, localDaysOfMonth, maxDayOfMonth)) {

                offset = AlarmUtils.getOffsetToNextOrEqual(currentDayOfMonth, AlarmUtils.MinDayOfMonth, maxDayOfMonth, localDaysOfMonth);
                alarm.add(Calendar.DAY_OF_MONTH, offset);
            }

            currentMonth = alarm.get(Calendar.MONTH);
            currentDayOfMonth = alarm.get(Calendar.DAY_OF_MONTH);
            maxDayOfMonth = alarm.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (currentMaxDayOfMonth != maxDayOfMonth) {
                currentMaxDayOfMonth = maxDayOfMonth;
                localDaysOfMonth = AlarmUtils.getDaysOfMonth(cronEntry.getDaysOfMonth(), maxDayOfMonth);
            }
        }
    }

    void updateDayOfWeekAndMonth(Calendar alarm) {
        int currentMonth = alarm.get(Calendar.MONTH);
        int currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK);
        int offset = 0;

        // loop until we have a valid day AND month (if current is invalid)
        while (!AlarmUtils.isIn(currentMonth, cronEntry.getMonths()) || !AlarmUtils.isIn(currentDayOfWeek, cronEntry.getDaysOfWeek())) {
            // if current month is invalid, advance to 1st day of next valid month
            if (!AlarmUtils.isIn(currentMonth, cronEntry.getMonths())) {
                offset = AlarmUtils.getOffsetToNextOrEqual(currentMonth, AlarmUtils.MinMonth, AlarmUtils.MaxMonth, cronEntry.getMonths());
                alarm.add(Calendar.MONTH, offset);
                alarm.set(Calendar.DAY_OF_MONTH, 1);
                currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK);
            }

            // advance to the next valid day of week, if necessary
            if (!AlarmUtils.isIn(currentDayOfWeek, cronEntry.getDaysOfWeek())) {
                offset = AlarmUtils.getOffsetToNextOrEqual(currentDayOfWeek, AlarmUtils.MinDayOfWeek, AlarmUtils.MaxDayOfWeek, cronEntry.getDaysOfWeek());
                alarm.add(Calendar.DAY_OF_YEAR, offset);
            }

            currentDayOfWeek = alarm.get(Calendar.DAY_OF_WEEK);
            currentMonth = alarm.get(Calendar.MONTH);
        }
    }

    /**
     * @return a string representation of this alarm.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("Alarm (uuid=" + getUUID() + " name=" + getName() + ") params");
        sb.append(" minute=");
        sb.append("{" + AlarmUtils.arrToString(cronEntry.getMinutes()) + "}");
        sb.append(" hour=");
        sb.append("{" + AlarmUtils.arrToString(cronEntry.getHours()) + "}");
        sb.append(" dayOfMonth=");
        sb.append("{" + AlarmUtils.arrToString(cronEntry.getDaysOfMonth()) + "}");
        sb.append(" month=");
        sb.append("{" + AlarmUtils.arrToString(cronEntry.getMonths()) + "}");
        sb.append(" dayOfWeek=");
        sb.append("{" + AlarmUtils.arrToString(cronEntry.getDaysOfWeek()) + "}");
        sb.append(" (next alarm date=" + new Date(getAlarmTime()) + ")");
        return sb.toString();
    }

    public String getCronString() {
        StringBuffer sb = new StringBuffer();
        sb.append(AlarmUtils.arrToString(cronEntry.getMinutes()));
        sb.append(" ");
        sb.append(AlarmUtils.arrToString(cronEntry.getHours()));
        sb.append(" ");
        sb.append(AlarmUtils.arrToString(cronEntry.getDaysOfMonth()));
        sb.append(" ");
        sb.append(AlarmUtils.arrToString(cronEntry.getMonths()));
        sb.append(" ");
        sb.append(AlarmUtils.arrToString(cronEntry.getDaysOfWeek()));
        return sb.toString();
    }
}
