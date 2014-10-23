/*
 *  com/jtheory/jdring/Test.java
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class run a bunch of tests.
 * 
 * @author Olivier Dedieu
 * @version 1.1, 09/13/1999
 */
public class Test {

    public static void main(String[] args) throws InterruptedException {

        SchedulingPattern pat1 = new SchedulingPattern();

        CronParameter cronEntry;
        try {
            cronEntry = pat1.parseCron("*/10 * * * * ");
            System.out.println(cronEntry);
        } catch (InvalidParameterException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            cronEntry = pat1.parseCron("1,4 1-20 1-2,5-6 0-11/3 * ");
            System.out.println(cronEntry);
        } catch (InvalidParameterException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            cronEntry = pat1.parseCron("1,4 1-20 1-2,5-6,L 0-11/3 * ");
            System.out.println(cronEntry);
        } catch (InvalidParameterException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println("GETTING OFFSETS");

        System.out.println("getOffsetToNext(3, 0, 11, new int[]{3,5,7,9}) = " + AlarmUtils.getOffsetToNext(3, 0, 11, new int[] { 3, 5, 7, 9 }));
        System.out.println("getOffsetToNextOrEqual(3, 0, 11, new int[]{3,5,7,9}) = " + AlarmUtils.getOffsetToNextOrEqual(3, 0, 11, new int[] { 3, 5, 7, 9 }));

        System.out.println();
        System.out.println("getOffsetToNext(9, 0, 11, new int[]{3,5,7,9}) = " + AlarmUtils.getOffsetToNext(9, 0, 11, new int[] { 3, 5, 7, 9 }));
        System.out.println("getOffsetToNextOrEqual(9, 0, 11, new int[]{3,5,7,9}) = " + AlarmUtils.getOffsetToNextOrEqual(9, 0, 11, new int[] { 3, 5, 7, 9 }));

        System.out.println();
        System.out.println("getOffsetToNext(0, 0, 11, new int[]{0}) = " + AlarmUtils.getOffsetToNext(0, 0, 11, new int[] { 0 }));
        System.out.println("getOffsetToNextOrEqual(0, 0, 11, new int[]{0}) = " + AlarmUtils.getOffsetToNextOrEqual(0, 0, 11, new int[] { 0 }));

        System.out.println();
        System.out.println("getOffsetToNext(5, 0, 11, new int[]{5}) = " + AlarmUtils.getOffsetToNext(5, 0, 11, new int[] { 5 }));
        System.out.println("getOffsetToNextOrEqual(5, 0, 11, new int[]{5}) = " + AlarmUtils.getOffsetToNextOrEqual(5, 0, 11, new int[] { 5 }));

        System.out.println();
        System.out.println("getOffsetToNext(0, 0, 11, new int[]{-1}) = " + AlarmUtils.getOffsetToNext(0, 0, 11, new int[] { -1 }));
        System.out.println("getOffsetToNextOrEqual(0, 0, 11, new int[]{-1}) = " + AlarmUtils.getOffsetToNextOrEqual(0, 0, 11, new int[] { -1 }));

        System.out.println();

        System.out.println();
        System.out.println("discardValuesOverMax(new int[]{0,1,2,3,4,5,6}, 4)) = "
                + AlarmUtils.arrToString(AlarmUtils.discardValuesOverMax(new int[] { 0, 1, 2, 3, 4, 5, 6 }, 4)));
        System.out.println("discardValuesOverMax(new int[]{0,1,2,3,4,5,6}, 6)) = "
                + AlarmUtils.arrToString(AlarmUtils.discardValuesOverMax(new int[] { 0, 1, 2, 3, 4, 5, 6 }, 6)));
        System.out.println("discardValuesOverMax(new int[]{0,1,2,3,4,5,6}, 0)) = "
                + AlarmUtils.arrToString(AlarmUtils.discardValuesOverMax(new int[] { 0, 1, 2, 3, 4, 5, 6 }, 0)));
        System.out.println("discardValuesOverMax(new int[]{0,1,2,3,4,5,6}, 7)) = "
                + AlarmUtils.arrToString(AlarmUtils.discardValuesOverMax(new int[] { 0, 1, 2, 3, 4, 5, 6 }, 7)));


        TimerAlarm ta;
        ta = new TimerAlarm("TimerAlarm", 60, 600, false, null);
        for (int ii =0; ii< 40; ii++){
            System.out.println(new Date(ta.getAlarmTime()));
            ta.updateAlarmTime();
        }
        
        
        
        try {
            DailyAlarm da = new DailyAlarm("d", "8:30", "9:45", 20);
            for (int ii =0; ii< 40; ii++){
                System.out.println(new Date(da.getAlarmTime()));
                da.updateAlarmTime();
            }
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            WeeklyAlarm wa = new WeeklyAlarm("w", new int[] { 1, 2, 3, 4, 5, 6, 7 }, "18:30", "19:00:20", 20, false, null);
            for (int ii =0; ii< 40; ii++){
                System.out.println(new Date(wa.getAlarmTime()));
                wa.updateAlarmTime();
            }
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            MonthlyAlarm ma = new MonthlyAlarm("w", new int[] { 1, 32 }, "8:30", "8:50", 10, false, null);
            for (int ii =0; ii< 40; ii++){
                System.out.println(new Date(ma.getAlarmTime()));
                ma.updateAlarmTime();
            }
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        CronAlarm ca;
        ca = new CronAlarm("ComplexCron2", new int[] { 16, 17, 18 }, new int[] { 16 }, new int[] { -1 }, new int[] { -1 }, new int[] { Calendar.SUNDAY },
                false, null);
        for (int ii =0; ii< 40; ii++){
            System.out.println(new Date(ca.getAlarmTime()));
            ca.updateAlarmTime();
        }

        CronAlarm ca2 = new CronAlarm("ComplexCron2", new int[] { 16, 17, 18 }, new int[] { 16 }, new int[] { 32 }, new int[] { -1 },
                new int[] { Calendar.SUNDAY }, false, null);
        for (int ii =0; ii< 40; ii++){
            System.out.println(new Date(ca2.getAlarmTime()));
            ca2.updateAlarmTime();
        }
        
        CronAlarm ca3 = new CronAlarm("VeryComplexCron", new int[] { 16, 17, 18 }, new int[] { 16 }, new int[] { 32 }, new int[] { Calendar.APRIL },
                new int[] { Calendar.SUNDAY }, false, null);
        for (int ii =0; ii< 40; ii++){
            System.out.println(new Date(ca3.getAlarmTime()));
            ca3.updateAlarmTime();
        }

        AlarmManager mgr = new AlarmManager(true, "JDRingAlarmManager", new TestRingExecutorImpl());

        long current = System.currentTimeMillis();
        System.out.println("Current date is " + new Date(current));

        AlarmListener listener = new AlarmListener() {
            public void handleAlarm(AlarmEntry entry) {
                System.out.println("\u0007fixed date alarm : (" + new Date() + ") (uuid=" + entry.getUUID() + "( name=" + entry.getName() + ")");
            }
        };

        // Date alarm
        try {
            mgr.addDateAlarm("test1", new Date(current + (60 * 1000)), false, listener);
            mgr.addDateAlarm("test2", new Date(current + (30 * 1000)), false, listener);
            mgr.addDateAlarm("test3", new Date(current + (40 * 1000)), false, listener);
            mgr.addDateAlarm("test4(", new Date(current + (20 * 1000)), false, listener);
            mgr.addDateAlarm("test5", new Date(current + (10 * 1000)), false, listener);
            mgr.addDateAlarm("test6", new Date(current + (50 * 1000)), false, listener);
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mgr.addDateAlarm("test7", new Date(System.currentTimeMillis() + 300000), false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007Fixed date 5 minutes later (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Calendar CalAlarmTime = Calendar.getInstance();
        CalAlarmTime.add(Calendar.WEEK_OF_YEAR, 1);
        try {
            mgr.addDateAlarm("test8", CalAlarmTime.getTime(), false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007Fixed date one week later (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Elapsed-time alarm
        try {
            mgr.addTimerAlarm("test9", 2, 1, false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007Relative 1 min (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Elapsed-time alarm 10,000 minutes
        try {
            mgr.addTimerAlarm("test10", 1, 1, false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007Relative 10,000 min (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Cron-like alarm (minute, hour, day of month, month, day of week, year)
        try {
            mgr.addCronAlarm("test11", -1, -1, -1, -1, -1, false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007Cron every minute (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Cron-like alarm (minute, hour, day of month, month, day of week, year)
        try {
            mgr.addCronAlarm("test12", new int[] { 26, 27, 29 }, new int[] { 12 }, new int[] { 19, 27 }, new int[] { -1 }, new int[] { Calendar.THURSDAY },
                    false, new AlarmListener() {
                        public void handleAlarm(AlarmEntry entry) {
                            System.out.println("\u0007Cron complex1 (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                        }
                    });

        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Cron-like alarm (minute, hour, day of month, month, day of week, year)
        try {
            mgr.addCronAlarm("ComplexCron2", new int[] { 16, 17, 18 }, new int[] { 16 }, new int[] { -1 }, new int[] { -1 }, new int[] { Calendar.SUNDAY },
                    false, new AlarmListener() {
                        public void handleAlarm(AlarmEntry entry) {
                            System.out.println("\u0007Cron complex2 (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                        }
                    });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*
         * mgr.addCronAlarm(3, -1, -1, -1, -1, new AlarmListener() { public void handleAlarm(AlarmEntry entry) { System.out.println("\u0007Every hour at 03' ("
         * + new Date() + ")"); } });
         * 
         * mgr.addCronAlarm(00, 12, -1, -1, -1, new AlarmListener() { public void handleAlarm(AlarmEntry entry) { System.out.println("\u0007Lunch time (" + new
         * Date() + ")"); } });
         * 
         * mgr.addCronAlarm(24, 15, 11, Calendar.AUGUST, -1, new AlarmListener() { public void handleAlarm(AlarmEntry entry) {
         * System.out.println("\u0007Valerie's birthday"); } });
         * 
         * mgr.addCronAlarm(30, 9, 1, -1, -1, new AlarmListener() { public void handleAlarm(AlarmEntry entry) {
         * System.out.println("\u0007On the first of every month at 9:30"); } });
         * 
         * mgr.addCronAlarm(00, 18, -1, -1, Calendar.FRIDAY, new AlarmListener() { public void handleAlarm(AlarmEntry entry) {
         * System.out.println("\u0007On every Friday at 18:00"); } });
         * 
         * mgr.addCronAlarm(0, 0, 1, Calendar.JANUARY, -1, new AlarmListener() { public void handleAlarm(AlarmEntry entry) {
         * System.out.println("\u0007Does it work ?"); } });
         */

        for (int jj = 0; jj < 1; jj++) {
            try {
                mgr.addDailyAlarm("dailyAlarm-" + jj, "08:20", "19:27", 1, false, new AlarmListener() {
                    public void handleAlarm(AlarmEntry entry) {
                        System.out.println("\u0007DailyAlarm (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                    }
                });
                Thread.sleep(1);
            } catch (PastDateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            } catch (InvalidParameterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            mgr.addWeeklyAlarm("WeeklyAlarm", new int[] { Calendar.WEDNESDAY, Calendar.FRIDAY }, "08:20", "19:45", 2, false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007WeeklyAlarm (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mgr.addMonthlyAlarm("MonthlyAlarm", new int[] { 20, 21 }, "08:20", "19:45:05.001", 3, false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007MonthlyAlarm (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mgr.addMonthlyAlarm("MonthlyAlarm LAST_DAY", new int[] { 20, AlarmUtils.LAST_DAY_OF_MONTH }, "08:20", "19:45:05.001", 3, false, new AlarmListener() {
                public void handleAlarm(AlarmEntry entry) {
                    System.out.println("\u0007MonthlyAlarm (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                }
            });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Cron-like alarm (minute, hour, day of month, month, day of week, year)
        try {
            mgr.addCronAlarm("ComplexCron2 LAST_DAY", new int[] { 16, 17, 18 }, new int[] { 16 }, new int[] { AlarmUtils.LAST_DAY_OF_MONTH }, new int[] { -1 },
                    new int[] { -1 }, false, new AlarmListener() {
                        public void handleAlarm(AlarmEntry entry) {
                            System.out.println("\u0007Cron complex2 (" + new Date() + ") (uuid=" + entry.getUUID() + ", name=" + entry.getName() + ")");
                        }
                    });
        } catch (PastDateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Here are the registered alarms: ");
        System.out.println("----------------------------");
        List list = mgr.getAllAlarms();
        for (Iterator it = list.iterator(); it.hasNext();) {
            System.out.println("- " + it.next());
        }
        System.out.println("----------------------------");

        for (int ii = 0; ii < 15; ii++) {
            Thread.sleep(60 * 1000);
            System.out.println("----Arret dans " + (15 - ii - 1) + "------------------------");
        }
        mgr.removeAllAlarmsAndStop();
    }
}

class TestRingExecutorImpl implements RingExecutor {

    private ExecutorService executorService;

    public void execute(Runnable r) throws NullPointerException {
        if (r==null)
            throw new NullPointerException();
        
        executorService.execute(r);
    }

    public TestRingExecutorImpl() {
        executorService = Executors.newFixedThreadPool(10);
    }
}
