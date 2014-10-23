/*
 * cron4j - A pure Java cron-like scheduler
 * 
 * Copyright (C) 2007-2010 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.jtheory.jdring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * <p>
 * A UNIX crontab-like pattern is a string split in five space separated parts. Each part is intented as:
 * </p>
 * <ol>
 * <li><strong>Minutes sub-pattern</strong>. During which minutes of the hour should the task been launched? The values range is from 0 to 59.</li>
 * <li><strong>Hours sub-pattern</strong>. During which hours of the day should the task been launched? The values range is from 0 to 23.</li>
 * <li><strong>Days of month sub-pattern</strong>. During which days of the month should the task been launched? The values range is from 1 to 31. The special
 * value L can be used to recognize the last day of month.</li>
 * <li><strong>Months sub-pattern</strong>. During which months of the year should the task been launched? The values range is from 1 (January) to 12
 * (December), otherwise this sub-pattern allows the aliases &quot;jan&quot;, &quot;feb&quot;, &quot;mar&quot;, &quot;apr&quot;, &quot;may&quot;,
 * &quot;jun&quot;, &quot;jul&quot;, &quot;aug&quot;, &quot;sep&quot;, &quot;oct&quot;, &quot;nov&quot; and &quot;dec&quot;.</li>
 * <li><strong>Days of week sub-pattern</strong>. During which days of the week should the task been launched? The values range is from 0 (Sunday) to 6
 * (Saturday), otherwise this sub-pattern allows the aliases &quot;sun&quot;, &quot;mon&quot;, &quot;tue&quot;, &quot;wed&quot;, &quot;thu&quot;,
 * &quot;fri&quot; and &quot;sat&quot;.</li>
 * </ol>
 * <p>
 * The star wildcard character is also admitted, indicating &quot;every minute of the hour&quot;, &quot;every hour of the day&quot;, &quot;every day of the
 * month&quot;, &quot;every month of the year&quot; and &quot;every day of the week&quot;, according to the sub-pattern in which it is used.
 * </p>
 * <p>
 * Once the scheduler is started, a task will be launched when the five parts in its scheduling pattern will be true at the same time.
 * </p>
 * <p>
 * Some examples:
 * </p>
 * <p>
 * <strong>5 * * * *</strong><br />
 * This pattern causes a task to be launched once every hour, at the begin of the fifth minute (00:05, 01:05, 02:05 etc.).
 * </p>
 * <p>
 * <strong>* * * * *</strong><br />
 * This pattern causes a task to be launched every minute.
 * </p>
 * <p>
 * <strong>* 12 * * Mon</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour of Monday.
 * </p>
 * <p>
 * <strong>* 12 16 * Mon</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour if the day is the 16th, of the month, and also every Monday.
 * </p>
 * <p>
 * Every sub-pattern can contain two or more comma separated values.
 * </p>
 * <p>
 * <strong>59 11 * * 1,2,3,4,5</strong><br />
 * This pattern causes a task to be launched at 11:59AM on Monday, Tuesday, Wednesday, Thursday and Friday.
 * </p>
 * <p>
 * Values intervals are admitted and defined using the minus character.
 * </p>
 * <p>
 * <strong>59 11 * * 1-5</strong><br />
 * This pattern is equivalent to the previous one.
 * </p>
 * <p>
 * The slash character can be used to identify step values within a range. It can be used both in the form <em>*&#47;c</em> and <em>a-b/c</em>. The subpattern
 * is matched every <em>c</em> values of the range <em>0,maxvalue</em> or <em>a-b</em>.
 * </p>
 * <p>
 * <strong>*&#47;5 * * * *</strong><br />
 * This pattern causes a task to be launched every 5 minutes (0:00, 0:05, 0:10, 0:15 and so on).
 * </p>
 * <p>
 * <strong>3-18&#47;5 * * * *</strong><br />
 * This pattern causes a task to be launched every 5 minutes starting from the third minute of the hour, up to the 18th (0:03, 0:08, 0:13, 0:18, 1:03, 1:08 and
 * so on).
 * </p>
 * <p>
 * <strong>*&#47;15 9-17 * * *</strong><br />
 * This pattern causes a task to be launched every 15 minutes between the 9th and 17th hour of the day (9:00, 9:15, 9:30, 9:45 and so on... note that the last
 * execution will be at 17:45).
 * </p>
 * <p>
 * All the fresh described syntax rules can be used together.
 * </p>
 * <p>
 * <strong>* 12 10-16&#47;2 * *</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour of the day, but only if the day is the 10th, the 12th, the 14th or the 16th of
 * the month.
 * </p>
 * <p>
 * <strong>* 12 1-15,17,20-25 * *</strong><br />
 * This pattern causes a task to be launched every minute during the 12th hour of the day, but the day of the month must be between the 1st and the 15th, the
 * 20th and the 25, or at least it must be the 17th.
 * </p>
 * 
 * @author Carlo Pelliccia
 * @since 2.0
 */
public class SchedulingPattern {

    /**
     * The parser for the minute values.
     */
    private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();

    /**
     * The parser for the hour values.
     */
    private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();

    /**
     * The parser for the day of month values.
     */
    private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();

    /**
     * The parser for the month values.
     */
    private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();

    /**
     * The parser for the day of week values.
     */
    private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();

    /**
     * Builds a SchedulingPattern parsing it from a string.
     * 
     * @param pattern
     *            The pattern as a crontab-like string.
     * @throws InvalidParameterException
     *             If the supplied string is not a valid pattern.
     */
    public SchedulingPattern() {
    }

    public CronParameter parseCron(String pattern) throws InvalidParameterException {
        StringTokenizer st2 = new StringTokenizer(pattern, " \t");
        CronParameter entry = new CronParameter();
        if (st2.countTokens() != 5) {
            throw new InvalidParameterException("invalid pattern: \"" + pattern + "\"");
        }
        try {
            entry.setMinutes(parseValue(st2.nextToken(), MINUTE_VALUE_PARSER));
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing minutes field: " + e.getMessage() + ".");
        }
        try {
            entry.setHours(parseValue(st2.nextToken(), HOUR_VALUE_PARSER));
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing hours field: " + e.getMessage() + ".");
        }
        try {
            entry.setDaysOfMonth(parseValue(st2.nextToken(), DAY_OF_MONTH_VALUE_PARSER));
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
        }
        try {
            entry.setMonths(parseValue(st2.nextToken(), MONTH_VALUE_PARSER));
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing months field: " + e.getMessage() + ".");
        }
        try {
            entry.setDaysOfWeek(parseValue(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER));
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing days of week field: " + e.getMessage() + ".");
        }
        return entry;
    }

    public int[] parseDayOfMonths(String pattern) throws InvalidParameterException {
        ArrayList values;
        try {
            values = parseValue(pattern, DAY_OF_MONTH_VALUE_PARSER);
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
        }
        return CronParameter.getTabInts(values);
    }

    public int[] parseDayOfWeeks(String pattern) throws InvalidParameterException {
        ArrayList values;
        try {
            values = parseValue(pattern, DAY_OF_WEEK_VALUE_PARSER);
        } catch (Exception e) {
            throw new InvalidParameterException("invalid pattern \"" + pattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
        }
        return CronParameter.getTabInts(values);
    }

    /**
     * A ValueMatcher utility builder.
     * 
     * @param str
     *            The pattern part for the ValueMatcher creation.
     * @param parser
     *            The parser used to parse the values.
     * @return The requested ValueMatcher.
     * @throws InvalidParameterException
     *             If the supplied pattern part is not valid.
     */
    private ArrayList parseValue(String str, ValueParser parser) throws InvalidParameterException {
        ArrayList values = new ArrayList();
        if (str.length() == 1 && str.equals("*")) {
            values.add(new Integer(-1));
        } else {

            StringTokenizer st = new StringTokenizer(str, ",");
            while (st.hasMoreTokens()) {
                String element = st.nextToken();
                ArrayList local;
                try {
                    local = parseListElement(element, parser);
                } catch (InvalidParameterException e) {
                    throw new InvalidParameterException("invalid field \"" + str + "\", invalid element \"" + element + "\", " + e.getMessage());
                }
                for (Iterator i = local.iterator(); i.hasNext();) {
                    Integer value = (Integer) i.next();
                    if (!values.contains(value)) {
                        values.add(value);
                    }
                }
            }
        }
        if (values.size() == 0) {
            throw new InvalidParameterException("invalid field \"" + str + "\"");
        }
        return values;
    }

    /**
     * Parses an element of a list of values of the pattern.
     * 
     * @param str
     *            The element string.
     * @param parser
     *            The parser used to parse the values.
     * @return A list of integers representing the allowed values.
     * @throws InvalidParameterException
     *             If the supplied pattern part is not valid.
     */
    private ArrayList parseListElement(String str, ValueParser parser) throws InvalidParameterException {
        StringTokenizer st = new StringTokenizer(str, "/");
        int size = st.countTokens();
        if (size < 1 || size > 2) {
            throw new InvalidParameterException("syntax error");
        }
        ArrayList values;
        try {
            values = parseRange(st.nextToken(), parser);
        } catch (InvalidParameterException e) {
            throw new InvalidParameterException("invalid range, " + e.getMessage());
        }
        if (size == 2) {
            String dStr = st.nextToken();
            int div;
            try {
                div = Integer.parseInt(dStr);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("invalid divisor \"" + dStr + "\"");
            }
            if (div < 1) {
                throw new InvalidParameterException("non positive divisor \"" + div + "\"");
            }
            ArrayList values2 = new ArrayList();
            for (int i = 0; i < values.size(); i += div) {
                values2.add(values.get(i));
            }
            return values2;
        } else {
            return values;
        }
    }

    /**
     * Parses a range of values.
     * 
     * @param str
     *            The range string.
     * @param parser
     *            The parser used to parse the values.
     * @return A list of integers representing the allowed values.
     * @throws InvalidParameterException
     *             If the supplied pattern part is not valid.
     */
    private ArrayList parseRange(String str, ValueParser parser) throws InvalidParameterException {
        if (str.equals("*")) {
            int min = parser.getMinValue();
            int max = parser.getMaxValue();
            ArrayList values = new ArrayList();
            for (int i = min; i <= max; i++) {
                values.add(new Integer(i));
            }
            return values;
        }
        StringTokenizer st = new StringTokenizer(str, "-");
        int size = st.countTokens();
        if (size < 1 || size > 2) {
            throw new InvalidParameterException("syntax error");
        }
        String v1Str = st.nextToken();
        int v1;
        try {
            v1 = parser.parse(v1Str);
        } catch (InvalidParameterException e) {
            throw new InvalidParameterException("invalid value \"" + v1Str + "\", " + e.getMessage());
        }
        if (size == 1) {
            ArrayList values = new ArrayList();
            values.add(new Integer(v1));
            return values;
        } else {
            String v2Str = st.nextToken();
            int v2;
            try {
                v2 = parser.parse(v2Str);
            } catch (InvalidParameterException e) {
                throw new InvalidParameterException("invalid value \"" + v2Str + "\", " + e.getMessage());
            }
            ArrayList values = new ArrayList();
            if (v1 < v2) {
                for (int i = v1; i <= v2; i++) {
                    values.add(new Integer(i));
                }
            } else if (v1 > v2) {
                int min = parser.getMinValue();
                int max = parser.getMaxValue();
                for (int i = v1; i <= max; i++) {
                    values.add(new Integer(i));
                }
                for (int i = min; i <= v2; i++) {
                    values.add(new Integer(i));
                }
            } else {
                // v1 == v2
                values.add(new Integer(v1));
            }
            return values;
        }
    }

    /**
     * This utility method changes an alias to an int value.
     * 
     * @param value
     *            The value.
     * @param aliases
     *            The aliases list.
     * @param offset
     *            The offset appplied to the aliases list indices.
     * @return The parsed value.
     * @throws InvalidParameterException
     *             If the expressed values doesn't match any alias.
     */
    static int parseAlias(String value, String[] aliases, int offset) throws InvalidParameterException {
        for (int i = 0; i < aliases.length; i++) {
            if (aliases[i].equalsIgnoreCase(value)) {
                return offset + i;
            }
        }
        throw new InvalidParameterException("invalid alias \"" + value + "\"");
    }

    /**
     * Definition for a value parser.
     */
    private static interface ValueParser {

        /**
         * Attempts to parse a value.
         * 
         * @param value
         *            The value.
         * @return The parsed value.
         * @throws InvalidParameterException
         *             If the value can't be parsed.
         */
        public int parse(String value) throws InvalidParameterException;

        /**
         * Returns the minimum value accepred by the parser.
         * 
         * @return The minimum value accepred by the parser.
         */
        public int getMinValue();

        /**
         * Returns the maximum value accepred by the parser.
         * 
         * @return The maximum value accepred by the parser.
         */
        public int getMaxValue();

    }

    /**
     * A simple value parser.
     */
    private static class SimpleValueParser implements ValueParser {

        /**
         * The minimum allowed value.
         */
        protected int minValue;

        /**
         * The maximum allowed value.
         */
        protected int maxValue;

        /**
         * Builds the value parser.
         * 
         * @param minValue
         *            The minimum allowed value.
         * @param maxValue
         *            The maximum allowed value.
         */
        public SimpleValueParser(int minValue, int maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public int parse(String value) throws InvalidParameterException {
            int i;
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("invalid integer value");
            }
            if (i < minValue || i > maxValue) {
                throw new InvalidParameterException("value out of range");
            }
            return i;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }

    }

    /**
     * The minutes value parser.
     */
    private static class MinuteValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public MinuteValueParser() {
            super(0, 59);
        }

    }

    /**
     * The hours value parser.
     */
    private static class HourValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public HourValueParser() {
            super(0, 23);
        }

    }

    /**
     * The days of month value parser.
     */
    private static class DayOfMonthValueParser extends SimpleValueParser {

        /**
         * Builds the value parser.
         */
        public DayOfMonthValueParser() {
            super(1, 31);
        }

        /**
         * Added to support last-day-of-month.
         * 
         * @param value
         *            The value to be parsed
         * @return the integer day of the month or 32 for last day of the month
         * @throws InvalidParameterException
         *             if the input value is invalid
         */
        public int parse(String value) throws InvalidParameterException {
            if (value.equalsIgnoreCase("L")) {
                return 32;
            } else {
                return super.parse(value);
            }
        }

    }

    /**
     * The value parser for the months field.
     */
    private static class MonthValueParser extends SimpleValueParser {

        /**
         * Months aliases.
         */
        private static String[] ALIASES = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

        /**
         * Builds the months value parser.
         */
        public MonthValueParser() {
            super(0, 11);
        }

        public int parse(String value) throws InvalidParameterException {
            try {
                // try as a simple value
                return super.parse(value);
            } catch (InvalidParameterException e) {
                // try as an alias
                return parseAlias(value, ALIASES, 0);
            }
        }

    }

    /**
     * The value parser for the months field.
     */
    private static class DayOfWeekValueParser extends SimpleValueParser {

        /**
         * Days of week aliases.
         */
        private static String[] ALIASES = { "sun", "mon", "tue", "wed", "thu", "fri", "sat" };

        /**
         * Builds the months value parser.
         */
        public DayOfWeekValueParser() {
            super(1, 7);
        }

        public int parse(String value) throws InvalidParameterException {
            try {
                // try as a simple value
                return super.parse(value);
            } catch (InvalidParameterException e) {
                // try as an alias
                return parseAlias(value, ALIASES, 1);
            }
        }

    }

}
