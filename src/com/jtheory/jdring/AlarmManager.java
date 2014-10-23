/*
 *  com/jtheory/jdring/AlarmManager.java
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
 *  History:
 *
 *  04/02/2004 Rob Whelan:
 *          - AlarmEntry, AlarmManager:
 *              - complete rewrite of updateAlarmTime() to support new features
 *              - added support for lists of values for each field (like cron)
 *              - changed handling of dayOfMonth and dayOfWeek to match logic
 *                in Vixie cron (if there are values for both, the closest is
 *                taken).
 *              - now AlarmEntry takes a name parameter - now two alarms can
 *                have the same schedule without confusing compareTo() and equals().
 *              - changed AlarmEntry compareTo() to also use the last updated time
 *                of an alarm to give priority if the alarmTimes match
 *              - changed AlarmEntry equals() method to only return true if
 *                name AND alarm date AND schedule details match.  Otherwise it's too
 *                easy to have alarms overlap -- and one will secretly never be run.
 *              - added method setRingInNewThread() to AlarmEntry.  By default
 *                all AlarmListeners are notified in a single Thread, so a
 *                long-running handleAlarm() method will cause other alarms to pile
 *                up, waiting.  Calling this method causes the given AlarmEntry's
 *                listener to be notified in a new Thread.
 *              - Fixed odd behavior caused by misuse of SortedSet.add()
 *              - Comments and some variable name changes for clarity
 *  06/15/2000 Olivier Dedieu:
 *	       - support deamon and named thread
 *             - clean the sources to support only JDK1.2 and above
 *
 *  05/21/2000 Olivier Dedieu:
 *             - AlarmEntry:
 *               - fixed a bug in comparable implementation (equals method)
 *               - implements Serializable
 *
 *  09/28/1999 Olivier Dedieu: fixed a bug in AlarmEntry when used with
 *             JDK1.1.8
 *
 *  09/27/1999 David Sims <david@simscomputing.com>:
 *             fixed a couple more bugs in AlarmManager.java and AlarmWaiter.java.
 *             (see changes)
 *
 *  09/13/1999 David Sims <david@simscomputing.com>: rewrites the
 *             class to use a TreeSet instead of the
 *             fr.dyade.util.PriorityQueue.
 *
 *  08/01/1999 Olivier Dedieu: creates the class
 * */

package com.jtheory.jdring;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class implements an alarm manager similar to Unix <code>cron</code> and <code>at</code> daemons. It is intended to fire events when alarms' date and
 * time match the current ones. Alarms are added dynamically and can be one-shot or repetitive (i.e. rescheduled when matched). Time unit is seconds. Alarms
 * scheduled less than one second to the current time are rejected (a <code>PastDateException</code> is thrown).
 * <p>
 * 
 * The alarm scheduler has been designed to manage a large quantity of alarms (it uses a priority queue to optimize alarm dates selection) and to reduce the use
 * of the CPU time (the AlarmManager's thread is started only when there are alarms to be managed and it sleeps until the next alarm date).
 * <p>
 * 
 * Note : because of clocks' skews some alarm dates may be erroneous, particularly if the next alarm date is scheduled for a remote time (e.g. more than a few
 * days). In order to avoid that problem, well-connected machines can use the <a href="ftp://ftp.inria.fr/rfc/rfc13xx/rfc1305.Z">Network Time Protocol</a> (NTP)
 * to synchronize their clock.
 * <p>
 * 
 * Example of use:
 * 
 * <pre>
 * // Creates a new AlarmManager
 * AlarmManager mgr = new AlarmManager();
 * 
 * // Date alarm (non repetitive)
 * mgr.addAlarm(&quot;fixed5min&quot;, new Date(System.currentTimeMillis() + 300000), new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;5 minutes later&quot;);
 *     }
 * });
 * 
 * Calendar cal = Calendar.getInstance();
 * cal.add(Calendar.WEEK_OF_YEAR, 1);
 * mgr.addAlarm(&quot;week_one&quot;, cal.getTime(), new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;One week later&quot;);
 *     }
 * });
 * 
 * // Alarm with a delay (in minute) relative to the current time.
 * mgr.addAlarm(1, true, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;1 more minute ! (&quot; + new Date() + &quot;)&quot;);
 *     }
 * });
 * 
 * // Cron-like alarm (minute, hour, day of month, month, day of week, year)
 * // Repetitive when the year is not specified.
 * 
 * mgr.addAlarm(-1, -1, -1, -1, -1, -1, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;Every minute (&quot; + new Date() + &quot;)&quot;);
 *     }
 * });
 * 
 * mgr.addAlarm(5, -1, -1, -1, -1, -1, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;Every hour at 5' (&quot; + new Date() + &quot;)&quot;);
 *     }
 * });
 * 
 * mgr.addAlarm(00, 12, -1, -1, -1, -1, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;Lunch time (&quot; + new Date() + &quot;)&quot;);
 *     }
 * });
 * 
 * mgr.addAlarm(07, 14, 1, Calendar.JANUARY, -1, -1, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;Happy birthday Lucas !&quot;);
 *     }
 * });
 * 
 * mgr.addAlarm(30, 9, 1, -1, -1, -1, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;On the first of every month at 9:30&quot;);
 *     }
 * });
 * 
 * mgr.addAlarm(00, 18, -1, -1, Calendar.FRIDAY, -1, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;On every Friday at 18:00&quot;);
 *     }
 * });
 * 
 * mgr.addAlarm(00, 13, 1, Calendar.AUGUST, -1, 2001, new AlarmListener() {
 *     public void handleAlarm(AlarmEntry entry) {
 *         System.out.println(&quot;2 years that this class was programmed !&quot;);
 *     }
 * });
 * </pre>
 * 
 * @author Olivier Dedieu, David Sims, Jim Lerner, Rob Whelan
 * @version 1.3, 15/06/2000
 */

public class AlarmManager {
    protected AlarmWaiter waiter;
    protected SortedSet queue;
    private RingExecutor ringEexecutor=null;

    /**
     * Creates a new AlarmManager. The waiter thread will be started only when the first alarm listener will be added.
     * 
     * @param isDaemon
     *            true if the waiter thread should run as a daemon.
     * @param threadName
     *            the name of the waiter thread
     */
    
    public AlarmManager(boolean isDaemon, String threadName, RingExecutor ringEexecutor) {
        queue = new TreeSet();
        waiter = new AlarmWaiter(this, isDaemon, threadName);
        this.ringEexecutor = ringEexecutor;
    }
    /**
     * Creates a new AlarmManager. The waiter thread will be started only when the first alarm listener will be added.
     * 
     * @param isDaemon
     *            true if the waiter thread should run as a daemon.
     * @param threadName
     *            the name of the waiter thread
     */
    public AlarmManager(boolean isDaemon, String threadName) {
        queue = new TreeSet();
        waiter = new AlarmWaiter(this, isDaemon, threadName);
    }

    /**
     * Creates a new AlarmManager. The waiter thread will be started only when the first alarm listener will be added. The waiter thread will <i>not</i> run as
     * a daemon.
     */
    public AlarmManager() {
        this(false, "AlarmManager");
    }

    /**
     * Adds an alarm for a specified date.
     * 
     * @param date
     *            the alarm date to be added.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past or less than 1 second closed to the current date).
     */
    public synchronized DateAlarm addDateAlarm(String _name, Date _date, boolean _ringInDedicatedThread, AlarmListener _listener) throws PastDateException {
        DateAlarm entry = new DateAlarm(_name, _date, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified delay.
     * 
     * @param delay
     *            the alarm delay in minute (relative to now).
     * @param _period
     *            periodMinutes the alarm period in minutes. (if <= 0 then the alarm it is not rescheduled when reached).
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second closed to the current date).
     */
    public synchronized TimerAlarm addTimerAlarm(String _name, int _delay, int _period, boolean _ringInDedicatedThread, AlarmListener _listener) throws PastDateException {
        TimerAlarm entry = new TimerAlarm(_name, _delay, _period, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified period.
     * 
     * @param start
     *            The start time of the period in the day (HH:MM:SS.mmm).
     * @param end
     *            The end time of the period in the day (HH:MM:SS.mmm).
     * @param period
     *            periodMinutes the alarm period in minutes.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second closed to the current date).
     * @throws InvalidParameterException
     */
    public synchronized DailyAlarm addDailyAlarm(String name, String start, String end, int period, boolean _ringInDedicatedThread, AlarmListener _listener)
            throws PastDateException, InvalidParameterException {
        DailyAlarm entry = new DailyAlarm(name, start, end, period, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified period.
     * 
     * @param days
     *            Days of the weeks (Calendar.SUNDAY to Calendar.SATURDAY).
     * @param start
     *            The start time of the period in the day (HH:MM:SS.mmm).
     * @param end
     *            The end time of the period in the day (HH:MM:SS.mmm).
     * @param period
     *            periodMinutes the alarm period in minutes.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second closed to the current date).
     * @throws InvalidParameterException
     */
    public synchronized WeeklyAlarm addWeeklyAlarm(String name, int[] days, String start, String end, int period, boolean _ringInDedicatedThread,
            AlarmListener _listener) throws PastDateException, InvalidParameterException {
        WeeklyAlarm entry = new WeeklyAlarm(name, days, start, end, period, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    public synchronized WeeklyAlarm addWeeklyAlarm(String name, String daysPattern, String start, String end, int period, boolean _ringInDedicatedThread,
            AlarmListener _listener) throws PastDateException, InvalidParameterException {
        WeeklyAlarm entry = new WeeklyAlarm(name, daysPattern, start, end, period, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified period.
     * 
     * @param days
     *            Days of the month : can be 32 for last day of the month.
     * @param start
     *            The start time of the period in the day (HH:MM:SS.mmm).
     * @param end
     *            The end time of the period in the day (HH:MM:SS.mmm).
     * @param period
     *            periodMinutes the alarm period in minutes.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second closed to the current date).
     * @throws InvalidParameterException
     */
    public synchronized MonthlyAlarm addMonthlyAlarm(String name, int[] days, String start, String end, int period, boolean _ringInDedicatedThread,
            AlarmListener _listener) throws PastDateException, InvalidParameterException {
        MonthlyAlarm entry = new MonthlyAlarm(name, days, start, end, period, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    public synchronized MonthlyAlarm addMonthlyAlarm(String name, String daysPattern, String start, String end, int period, boolean _ringInDedicatedThread,
            AlarmListener _listener) throws PastDateException, InvalidParameterException {
        MonthlyAlarm entry = new MonthlyAlarm(name, daysPattern, start, end, period, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified date.
     * 
     * @param minute
     *            minute of the alarm. Allowed values 0-59, or -1 for all.
     * @param hour
     *            hour of the alarm. Allowed values 0-23, or -1 for all.
     * @param dayOfMonth
     *            day of month of the alarm. Allowed values 1-7 (1 = Sunday, 2 = Monday, ...), or -1 for all. <code>java.util.Calendar</code> constants can be
     *            used.
     * @param month
     *            month of the alarm. Allowed values 0-11 (0 = January, 1 = February, ...), or -1 for all. <code>java.util.Calendar</code> constants can be
     *            used.
     * @param dayOfWeek
     *            day of week of the alarm. Allowed values 1-31, or -1 for all.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second away from the current date).
     */
    public synchronized CronAlarm addCronAlarm(String _name, int _minute, int _hour, int _dayOfMonth, int _month, int _dayOfWeek,
            boolean _ringInDedicatedThread, AlarmListener _listener) throws PastDateException {

        CronAlarm entry = new CronAlarm(_name, _minute, _hour, _dayOfMonth, _month, _dayOfWeek, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified date or matching dates (for unrestricted fields).
     * 
     * @param _ringInDedicatedThread
     * 
     * @param minutes
     *            minutes of the alarm. Allowed values 0-59, or -1 for all.
     * @param hours
     *            hours of the alarm. Allowed values 0-23, or -1 for all.
     * @param daysOfMonth
     *            days of month of the alarm. Allowed values 1-7 (1 = Sunday, 2 = Monday, ...), or -1 for all. <code>java.util.Calendar</code> constants can be
     *            used.
     * @param months
     *            months of the alarm. Allowed values 0-11 (0 = January, 1 = February, ...), or -1 for all. <code>java.util.Calendar</code> constants can be
     *            used.
     * @param daysOfWeek
     *            days of week of the alarm. Allowed values 1-31, or -1 for all.
     * @param listener
     *            the alarm listener.
     * @return the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than 1 second away from the current date).
     */
    public synchronized CronAlarm addCronAlarm(String _name, int[] _minutes, int[] _hours, int[] _daysOfMonth, int[] _months, int[] _daysOfWeek,
            boolean _ringInDedicatedThread, AlarmListener _listener) throws PastDateException {

        CronAlarm entry = new CronAlarm(_name, _minutes, _hours, _daysOfMonth, _months, _daysOfWeek, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    public synchronized CronAlarm addCronAlarm(String _name, String pattern, boolean _ringInDedicatedThread, AlarmListener _listener) throws PastDateException,
            InvalidParameterException {

        CronAlarm entry = new CronAlarm(_name, pattern, _ringInDedicatedThread, _listener);
        addAlarm(entry);
        return entry;
    }

    /**
     * Adds an alarm for a specified AlarmEntry
     * 
     * @param entry
     *            the AlarmEntry.
     * @exception PastDateException
     *                if the alarm date is in the past (or less than one second away from the current date).
     */
    public synchronized void addAlarm(AlarmEntry _entry) throws PastDateException {
        Logger.debug("Add a new alarm entry : " + _entry);

        _entry.checkAlarmTime();
        queue.add(_entry);
        if (queue.first().equals(_entry)) {
            Logger.debug("This new alarm is the top one, update the waiter thread");
            waiter.update(_entry.getAlarmTime());
        }
    }

    /**
     * Removes the specified AlarmEntry.
     * 
     * @param entry
     *            the AlarmEntry that needs to be removed.
     * @return <code>true</code> if there was an alarm for this date, <code>false</code> otherwise.
     */
    public synchronized boolean removeAlarm(AlarmEntry _entry) {

        boolean found = false;

        if (!queue.isEmpty()) {
            AlarmEntry was_first = (AlarmEntry) queue.first();
            found = queue.remove(_entry);

            // update the queue if it's not now empty, and the first alarm has changed
            if (!queue.isEmpty() && _entry.equals(was_first)) {
                waiter.update(((AlarmEntry) queue.first()).getAlarmTime());
            }
        }

        return found;
    } // removeAlarm()

    /**
     * Removes all the alarms. No more alarms, even newly added ones, will be fired.
     */
    public synchronized void removeAllAlarms() {
        queue.clear();
    }

    /**
     * Removes all the alarms. No more alarms, even newly added ones, will be fired.
     */
    public synchronized void removeAllAlarmsAndStop() {
        waiter.stop();
        waiter = null;
        queue.clear();
    }

    public boolean isStopped() {
        return (waiter == null);
    }

    /**
     * Tests whether the supplied AlarmEntry is in the manager.
     * 
     * @param CronAlarm
     * @return boolean whether AlarmEntry is contained within the manager
     */
    public synchronized boolean containsAlarm(CronAlarm _alarmEntry) {
        return queue.contains(_alarmEntry);
    }

    /**
     * Returns a copy of all alarms in the manager.
     */
    public synchronized List getAllAlarms() {
        List result = new ArrayList();

        Iterator iterator = queue.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }

        return result;
    }

    /**
     * This is method is called when an alarm date is reached. It is only be called by the the AlarmWaiter or by itself (if the next alarm is less than 1 second
     * away).
     */
    protected synchronized void ringNextAlarm() {
        Logger.debug("ringing next alarm");

        // if the queue is empty, there's nothing to do
        if (queue.isEmpty()) {
            return;
        }

        // Removes this alarm and notifies the listener
        AlarmEntry entry = (AlarmEntry) queue.first();
        queue.remove(entry);

        // NOTE: if the entry is still running when its next alarm time comes up,
        // that execution of the entry will be skipped.
        if (entry.isRingInDedicatedThread()) {
            new Thread(new RunnableRinger(entry)).start();
        } else {
            if (ringEexecutor != null){
                ringEexecutor.execute(new RunnableRinger(entry));
            }else{
                // ring in same thread, sequentially.. can delay other alarms
                try {
                    entry.ringAlarm();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        }

        // Reactivates the alarm if it is repetitive
        if (entry.updateAlarmTime())
            queue.add(entry);

        // Notifies the AlarmWaiter thread for the next alarm
        if (queue.isEmpty()) {
            Logger.debug("no more alarms to handle; queue is empty");
        } else {
            long alarmTime = ((AlarmEntry) queue.first()).getAlarmTime();
            if (alarmTime - System.currentTimeMillis() < 1000) {
                Logger.debug("next alarm is within 1 sec or already past - ring it without waiting");
                ringNextAlarm();
            } else {
                Logger.debug("updating the waiter for next alarm: " + queue.first());
                waiter.restart(alarmTime);
            }
        }
    } // notifyListeners()

    /**
     * Stops the waiter thread before ending.
     */
    public void finalize() {
        if (waiter != null)
            waiter.stop();
    }

    /**
     * Used to ring an AlarmEntry in a new Thread.
     * 
     * @see com.jtheory.jdring.CronAlarm#setRingInNewThread()
     */
    private class RunnableRinger implements Runnable {
        AlarmEntry entry = null;

        RunnableRinger(AlarmEntry _entry) {
            entry = _entry;
        }

        public void run() {
            try {
                entry.ringAlarm();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
