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

import java.util.ArrayList;

/**
 * @author jmelian
 * 
 */
public class CronParameter {

    public CronParameter(){
        
    }
    
    public CronParameter(int[] minutes, int[] hours, int[] daysOfMonth, int[] months, int[] daysOfWeek){
        this.minutes = minutes;
        this.hours = hours;
        this.daysOfMonth = daysOfMonth;
        this.months = months;
        this.daysOfWeek = daysOfWeek;
    }
    
    public static int[] getTabInts(ArrayList list) {
        int[] ints = new int[list.size()];
        for (int ii = 0; ii < ints.length; ii++) {
            ints[ii] = ((Integer) list.get(ii)).intValue();
        }
        return ints;
    }

    /**
     * The ValueMatcher list for the "minute" field.
     */
    private int[] minutes = { -1 };

    /**
     * The ValueMatcher list for the "hour" field.
     */
    private int[] hours = { -1 };

    /**
     * The ValueMatcher list for the "day of month" field.
     */
    private int[] daysOfMonth = { -1 };

    /**
     * The ValueMatcher list for the "month" field.
     */
    private int[] months = { -1 };

    /**
     * The ValueMatcher list for the "day of week" field.
     */
    private int[] daysOfWeek = { -1 };

    /**
     * @return the dayOfMonths
     */
    public int[] getDaysOfMonth() {
        return daysOfMonth;
    }

    /**
     * @return the dayOfWeeks
     */
    public int[] getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * @return the hours
     */
    public int[] getHours() {
        return hours;
    }

    /**
     * @return the minutes
     */
    public int[] getMinutes() {
        return minutes;
    }

    /**
     * @return the months
     */
    public int[] getMonths() {
        return months;
    }

    /**
     * @param dayOfMonths
     *            the dayOfMonths to set
     */
    public void setDaysOfMonth(int[] daysOfMonth) {
        this.daysOfMonth = daysOfMonth;
    }

    /**
     * @param dayOfMonths
     *            the dayOfMonths to set
     */
    public void setDaysOfMonth(ArrayList daysOfMonth) {
        this.daysOfMonth = getTabInts(daysOfMonth);
    }

    /**
     * @param daysOfWeek
     *            the dayOfWeeks to set
     */
    public void setDaysOfWeek(int[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    /**
     * @param daysOfWeek
     *            the dayOfWeeks to set
     */
    public void setDaysOfWeek(ArrayList daysOfWeek) {
        this.daysOfWeek = getTabInts(daysOfWeek);
    }

    /**
     * @param hours
     *            the hours to set
     */
    public void setHours(int[] hours) {
        this.hours = hours;
    }

    /**
     * @param hours
     *            the hours to set
     */
    public void setHours(ArrayList hours) {
        this.hours = getTabInts(hours);
    }

    /**
     * @param minutes
     *            the minutes to set
     */
    public void setMinutes(ArrayList minutes) {
        this.minutes = getTabInts(minutes);
    }

    public void setMinutes(int[] minutes) {
        this.minutes = minutes;
    }

    /**
     * @param months
     *            the months to set
     */
    public void setMonths(int[] months) {
        this.months = months;
    }

    /**
     * @param months
     *            the months to set
     */
    public void setMonths(ArrayList months) {
        this.months = getTabInts(months);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        tabToString(getMinutes(), sb);
        sb.append(" ");
        tabToString(getHours(), sb);
        sb.append(" ");
        tabToString(getDaysOfMonth(), sb);
        sb.append(" ");
        tabToString(getMonths(), sb);
        sb.append(" ");
        tabToString(getDaysOfWeek(), sb);
        return sb.toString();

    }

    private void tabToString(int[] tab, StringBuffer sb) {
        if (tab == null)
            sb.append("-1");
        else {
            for (int ii = 0; ii < tab.length; ii++) {
                if (ii > 0)
                    sb.append(",");
                sb.append(tab[ii]);
            }
        }

    }

}
