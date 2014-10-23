/*
 *  com/jtheory/jdring/AlarmUtils.java
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

public class AlarmUtils {

    public static final int MinDayOfWeek = 1;
    public static final int MaxDayOfWeek = 7;
    public static final int MinDayOfMonth = 1;
    public static final int MinHour = 0;
    public static final int MaxHour = 23;
    public static final int MinMonth = 0;
    public static final int MaxMonth = 11;

    public final static int LAST_DAY_OF_MONTH = 32;

    /**
     * handles -1 in values as * and returns true otherwise returns true iff given value is in the array
     */
    public static boolean isIn(int find, int[] values) {
        if ((values == null) || (values.length == 0))
            return true;

        if (values[0] == -1) {
            return true;
        } else {
            for (int i = 0; i < values.length; i++) {
                if (find == values[i])
                    return true;
            }
            return false;
        }
    }

    public static boolean isIn(int find, int[] values, int maxDayOfMonth) {
        if ((values == null) || (values.length == 0))
            return true;

        if (values[0] == -1) {
            return true;
        } else {
            for (int i = 0; i < values.length; i++) {
                if (find == values[i])
                    return true;
                else if ((values[i] == LAST_DAY_OF_MONTH) && (find == maxDayOfMonth))
                    return true;
            }
            return false;
        }
    }

    /**
     * if values = {-1} offset is 1 (because next value definitely matches) if current < last(values) offset is diff to next valid value if current >=
     * last(values) offset is diff to values[0], wrapping from max to min
     */
    static int getOffsetToNext(int current, int min, int max, int[] values) {
        int offset = 0;

        // find the distance to the closest valid value > current (wrapping if neccessary)

        // {-1} means * -- offset is 1 because current++ is valid value
        if (values[0] == -1) {
            offset = 1;
        } else {
            // need to wrap
            if (current >= last(values)) {
                int next = values[0];
                offset = (max - current + 1) + (next - min);
            } else // current < max(values) -- find next valid value after current
            {
                for (int i = 0; i < values.length; i++) {
                    if (current < values[i]) {
                        offset = values[i] - current;
                        break;
                    }
                }
            } // end current < max(values)
        }

        return offset;
    }

    /**
     * if values = {-1} or current is valid offset is 0. if current < last(values) offset is diff to next valid value if current >= last(values) offset is diff
     * to values[0], wrapping from max to min
     */
    public static int getOffsetToNextOrEqual(int current, int min, int max, int[] values) {
        int offset = 0;
        int[] safeValues = null;

        // find the distance to the closest valid value >= current (wrapping if necessary)

        // {-1} means * -- offset is 0 if current is valid value
        if (values[0] == -1 || isIn(current, values)) {
            offset = 0;
        } else {
            safeValues = discardValuesOverMax(values, max);

            // need to wrap
            if (current > last(safeValues)) {
                int next = safeValues[0];
                offset = (max - current + 1) + (next - min);
            } else // current <= max(values) -- find next valid value
            {
                for (int i = 0; i < values.length; i++) {
                    if (current < safeValues[i]) {
                        offset = safeValues[i] - current;
                        break;
                    }
                }
            } // end current <= max(values)
        }

        return offset;
    }

    /**
     * Assumes inputted values are not null, have at least one value, and are in ascending order.
     * 
     * @return copy of values without any trailing values that exceed the max
     */
    public static int[] discardValuesOverMax(int[] values, int max) {
        int[] safeValues = null;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                safeValues = new int[i];
                System.arraycopy(values, 0, safeValues, 0, i);
                return safeValues;
            }
        }
        return values;
    }

    /**
     * @return the last int in the array
     */
    public static int last(int[] intArray) {
        return intArray[intArray.length - 1];
    }

    public static String arrToString(int[] intArray) {
        if (intArray == null)
            return "null";
        if (intArray.length == 0)
            return "";

        StringBuilder  builder = new StringBuilder();
        for (int i = 0; i < intArray.length - 1; i++) {
            builder.append(String.valueOf(intArray[i])).append(", ");
        }
        builder.append(String.valueOf(intArray[intArray.length - 1]));

        return builder.toString();
    }

    public static int[] getDaysOfMonth(int[] daysOfMonth1, int lastDay) {

        if (AlarmUtils.isIn(AlarmUtils.LAST_DAY_OF_MONTH, daysOfMonth1)) {
            int[] local_daysOfMonth = new int[daysOfMonth1.length];
            System.arraycopy(daysOfMonth1, 0, local_daysOfMonth, 0, daysOfMonth1.length);
            for (int ii = 0; ii < local_daysOfMonth.length; ii++) {
                if (local_daysOfMonth[ii] == AlarmUtils.LAST_DAY_OF_MONTH)
                    local_daysOfMonth[ii] = lastDay;
            }
            return local_daysOfMonth;
        } else
            return daysOfMonth1;
    }

}
