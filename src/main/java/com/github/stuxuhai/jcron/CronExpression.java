/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import com.github.stuxuhai.jcron.AbstractPaser.DurationField;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

public class CronExpression {

    private String cronExp;
    private List<AbstractPaser> secondPasers;
    private List<AbstractPaser> minutePasers;
    private List<AbstractPaser> hourPasers;
    private List<AbstractPaser> dayOfMonthPasers;
    private List<AbstractPaser> monthPasers;
    private List<AbstractPaser> dayOfWeekPasers;
    private List<AbstractPaser> yearPasers;

    private static final Range<Integer> SECOND_RANGE = Range.closed(0, 59);
    private static final Range<Integer> MINUTE_RANGE = Range.closed(0, 59);
    private static final Range<Integer> HOUR_RANGE = Range.closed(0, 23);
    private static final Range<Integer> DAY_OF_MONTH_RANGE = Range.closed(1, 31);
    private static final Range<Integer> MONTH_RANGE = Range.closed(1, 12);
    private static final Range<Integer> DAY_OF_WEEK_RANGE = Range.closed(1, 7);
    private static final Range<Integer> YEAR_RANGE = Range.closed(1970, 2099);

    public CronExpression(String cronExp) {
        this.cronExp = cronExp;

        secondPasers = new ArrayList<AbstractPaser>();
        secondPasers.add(new PoundSignPaser(SECOND_RANGE, DurationField.SECOND));
        secondPasers.add(new RangePaser(SECOND_RANGE, DurationField.SECOND));
        secondPasers.add(new StepPaser(SECOND_RANGE, DurationField.SECOND));
        secondPasers.add(new SinglePaser(SECOND_RANGE, DurationField.SECOND));

        minutePasers = new ArrayList<AbstractPaser>();
        minutePasers.add(new PoundSignPaser(MINUTE_RANGE, DurationField.MINUTE));
        minutePasers.add(new RangePaser(MINUTE_RANGE, DurationField.MINUTE));
        minutePasers.add(new StepPaser(MINUTE_RANGE, DurationField.MINUTE));
        minutePasers.add(new SinglePaser(MINUTE_RANGE, DurationField.MINUTE));

        hourPasers = new ArrayList<AbstractPaser>();
        hourPasers.add(new PoundSignPaser(HOUR_RANGE, DurationField.HOUR));
        hourPasers.add(new RangePaser(HOUR_RANGE, DurationField.HOUR));
        hourPasers.add(new StepPaser(HOUR_RANGE, DurationField.HOUR));
        hourPasers.add(new SinglePaser(HOUR_RANGE, DurationField.HOUR));

        dayOfMonthPasers = new ArrayList<AbstractPaser>();
        dayOfMonthPasers.add(new PoundSignPaser(DAY_OF_MONTH_RANGE, DurationField.DAY_OF_MONTH));
        dayOfMonthPasers.add(new RangePaser(DAY_OF_MONTH_RANGE, DurationField.DAY_OF_MONTH));
        dayOfMonthPasers.add(new StepPaser(DAY_OF_MONTH_RANGE, DurationField.DAY_OF_MONTH));
        dayOfMonthPasers.add(new LastDayOfMonthPaser(DAY_OF_MONTH_RANGE, DurationField.DAY_OF_MONTH));
        dayOfMonthPasers.add(new NearestWeekdayOfMonthPaser(DAY_OF_MONTH_RANGE, DurationField.DAY_OF_MONTH));
        dayOfMonthPasers.add(new SinglePaser(DAY_OF_MONTH_RANGE, DurationField.DAY_OF_MONTH));

        monthPasers = new ArrayList<AbstractPaser>();
        monthPasers.add(new PoundSignPaser(MONTH_RANGE, DurationField.MONTH));
        monthPasers.add(new RangePaser(MONTH_RANGE, DurationField.MONTH));
        monthPasers.add(new StepPaser(MONTH_RANGE, DurationField.MONTH));
        monthPasers.add(new SinglePaser(MONTH_RANGE, DurationField.MONTH));

        dayOfWeekPasers = new ArrayList<AbstractPaser>();
        dayOfWeekPasers.add(new PoundSignPaser(DAY_OF_WEEK_RANGE, DurationField.DAY_OF_WEEK));
        dayOfWeekPasers.add(new RangePaser(DAY_OF_WEEK_RANGE, DurationField.DAY_OF_WEEK));
        dayOfWeekPasers.add(new StepPaser(DAY_OF_WEEK_RANGE, DurationField.DAY_OF_WEEK));
        dayOfWeekPasers.add(new LastDayOfMonthPaser(DAY_OF_WEEK_RANGE, DurationField.DAY_OF_WEEK));
        dayOfWeekPasers.add(new AsteriskPaser(DAY_OF_WEEK_RANGE, DurationField.DAY_OF_WEEK));
        dayOfWeekPasers.add(new SinglePaser(DAY_OF_WEEK_RANGE, DurationField.DAY_OF_WEEK));

        yearPasers = new ArrayList<AbstractPaser>();
        yearPasers.add(new PoundSignPaser(YEAR_RANGE, DurationField.YEAR));
        yearPasers.add(new RangePaser(YEAR_RANGE, DurationField.YEAR));
        yearPasers.add(new StepPaser(YEAR_RANGE, DurationField.YEAR));
        yearPasers.add(new SinglePaser(YEAR_RANGE, DurationField.YEAR));
    }

    private void validate(String[] exp) throws ParseException {
        if (exp.length != 7) {
            throw new ParseException("Unexpected end of expression.", -1);
        } else if ("?".equals(exp[DurationField.DAY_OF_MONTH.index]) && "?".equals(exp[DurationField.DAY_OF_WEEK.index])) {
            throw new ParseException("'?' can only be specfied for day-of-month or day-of-week.", -1);
        } else if (!"?".equals(exp[DurationField.DAY_OF_MONTH.index]) && !"?".equals(exp[DurationField.DAY_OF_WEEK.index])) {
            throw new ParseException("Support for specifying both a day-of-week and a day-of-month parameter is not implemented.", -1);
        } else if ("2".equals(exp[DurationField.MONTH.index]) && CharMatcher.DIGIT.matchesAllOf(exp[DurationField.DAY_OF_MONTH.index])) {
            int dayOfMonth = Integer.parseInt(exp[DurationField.DAY_OF_MONTH.index]);
            if (dayOfMonth > 29) {
                throw new ParseException("When month is 2, day-of-month should be in range [1, 29].", -1);
            }
        }
    }

    private String[] appendYearField(String[] exp) {
        if (exp.length == 6) {
            String[] newExp = new String[7];
            System.arraycopy(exp, 0, newExp, 0, exp.length);
            newExp[DurationField.YEAR.index] = "*";
            return newExp;
        }

        return exp;
    }

    private int searchNotLessThanIndex(List<Integer> sortedList, int value) {
        for (int i = 0, len = sortedList.size(); i < len; i++) {
            if (sortedList.get(i) >= value) {
                return i;
            }
        }

        return 0;
    }

    private int searchNotGreaterThanIndex(List<Integer> sortedList, int value) {
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            if (sortedList.get(i) <= value) {
                return i;
            }
        }

        return sortedList.size() - 1;
    }

    private List<Integer> parse(List<AbstractPaser> pasers, String partCronExp, DateTime dateTime, DurationField type) throws ParseException {
        Set<Integer> result = new HashSet<Integer>();
        for (String str : Splitter.on(",").omitEmptyStrings().split(partCronExp)) {
            boolean isMatch = false;
            for (AbstractPaser paser : pasers) {
                if (paser.matches(str)) {
                    Set<Integer> value = paser.parse(dateTime);
                    if (value != null) {
                        result.addAll(value);
                    }
                    isMatch = true;
                    break;
                }
            }

            if (!isMatch) {
                throw new ParseException(String.format("Invalid value of %s: %s.", type.name, str), -1);
            }
        }

        return Ordering.natural().sortedCopy(result);
    }

    private List<Integer> parseDayValueList(String[] fixedCronExp, DateTime dateTime) throws ParseException {
        List<Integer> dayValues = null;
        if ("?".equals(fixedCronExp[DurationField.DAY_OF_MONTH.index])) {
            dayValues = parse(dayOfWeekPasers, fixedCronExp[DurationField.DAY_OF_WEEK.index], dateTime, DurationField.DAY_OF_WEEK);
        } else {
            dayValues = parse(dayOfMonthPasers, fixedCronExp[DurationField.DAY_OF_MONTH.index], dateTime, DurationField.DAY_OF_MONTH);
        }

        return dayValues;
    }

    public DateTime getTimeAfter(DateTime dateTime) throws ParseException {
        String[] fixedCronExp = appendYearField(cronExp.split("\\s+"));
        validate(fixedCronExp);

        MutableDateTime mdt = dateTime.toMutableDateTime();
        mdt.setMillisOfSecond(0);

        List<Integer> secondValues = parse(secondPasers, fixedCronExp[DurationField.SECOND.index], dateTime, DurationField.SECOND);
        List<Integer> minuteValues = parse(minutePasers, fixedCronExp[DurationField.MINUTE.index], dateTime, DurationField.MINUTE);
        List<Integer> hourValues = parse(hourPasers, fixedCronExp[DurationField.HOUR.index], dateTime, DurationField.HOUR);
        List<Integer> monthValues = parse(monthPasers, fixedCronExp[DurationField.MONTH.index], dateTime, DurationField.MONTH);
        List<Integer> yearValues = parse(yearPasers, fixedCronExp[DurationField.YEAR.index], dateTime, DurationField.YEAR);

        int yearStartIndex = searchNotLessThanIndex(yearValues, mdt.getYear());
        for (int yearIndex = yearStartIndex, yearLen = yearValues.size(); yearIndex < yearLen; yearIndex++) {
            int year = yearValues.get(yearIndex);
            mdt.setYear(year);
            int monthStartIndex = (year == dateTime.getYear()) ? searchNotLessThanIndex(monthValues, dateTime.getMonthOfYear()) : 0;

            for (int monthIndex = monthStartIndex, monthLen = monthValues.size(); monthIndex < monthLen; monthIndex++) {
                int month = monthValues.get(monthIndex);
                mdt.setMonthOfYear(month);
                List<Integer> dayValues = parseDayValueList(fixedCronExp, mdt.toDateTime());
                int dayStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear())
                        ? searchNotLessThanIndex(dayValues, dateTime.getDayOfMonth()) : 0;

                for (int dayIndex = dayStartIndex, dayLen = dayValues.size(); dayIndex < dayLen; dayIndex++) {
                    int day = dayValues.get(dayIndex);
                    int maxDayOfMonth = mdt.toDateTime().dayOfMonth().withMaximumValue().toLocalDate().getDayOfMonth();
                    if (day > maxDayOfMonth) {
                        break;
                    }
                    mdt.setDayOfMonth(day);
                    int hourStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear() && day == dateTime.getDayOfMonth())
                            ? searchNotLessThanIndex(hourValues, dateTime.getHourOfDay()) : 0;

                    for (int hourIndex = hourStartIndex, hourLen = hourValues.size(); hourIndex < hourLen; hourIndex++) {
                        int hour = hourValues.get(hourIndex);
                        mdt.setHourOfDay(hour);
                        int minuteStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear() && day == dateTime.getDayOfMonth()
                                && hour == dateTime.getHourOfDay()) ? searchNotLessThanIndex(minuteValues, dateTime.getMinuteOfHour()) : 0;

                        for (int minuteIndex = minuteStartIndex, minuteLen = minuteValues.size(); minuteIndex < minuteLen; minuteIndex++) {
                            int minute = minuteValues.get(minuteIndex);
                            int secondStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear()
                                    && day == dateTime.getDayOfMonth() && hour == dateTime.getHourOfDay() && minute == dateTime.getMinuteOfHour())
                                            ? searchNotLessThanIndex(secondValues, dateTime.getSecondOfMinute()) : 0;
                            mdt.setMinuteOfHour(minute);
                            for (int secondIndex = secondStartIndex, secondLen = secondValues.size(); secondIndex < secondLen; secondIndex++) {
                                int second = secondValues.get(secondIndex);
                                mdt.setSecondOfMinute(second);
                                if (mdt.isAfter(dateTime)) {
                                    return mdt.toDateTime();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public DateTime getTimeBefore(DateTime dateTime) throws ParseException {
        String[] fixedCronExp = appendYearField(cronExp.split("\\s+"));
        validate(fixedCronExp);

        MutableDateTime mdt = dateTime.toMutableDateTime();
        mdt.setMillisOfSecond(0);

        List<Integer> secondValues = parse(secondPasers, fixedCronExp[DurationField.SECOND.index], dateTime, DurationField.SECOND);
        List<Integer> minuteValues = parse(minutePasers, fixedCronExp[DurationField.MINUTE.index], dateTime, DurationField.MINUTE);
        List<Integer> hourValues = parse(hourPasers, fixedCronExp[DurationField.HOUR.index], dateTime, DurationField.HOUR);
        List<Integer> monthValues = parse(monthPasers, fixedCronExp[DurationField.MONTH.index], dateTime, DurationField.MONTH);
        List<Integer> yearValues = parse(yearPasers, fixedCronExp[DurationField.YEAR.index], dateTime, DurationField.YEAR);

        int yearStartIndex = searchNotGreaterThanIndex(yearValues, mdt.getYear());
        for (int yearIndex = yearStartIndex; yearIndex >= 0; yearIndex--) {
            int year = yearValues.get(yearIndex);
            mdt.setYear(year);
            int monthStartIndex = (year == dateTime.getYear()) ? searchNotGreaterThanIndex(monthValues, dateTime.getMonthOfYear())
                    : monthValues.size() - 1;

            for (int monthIndex = monthStartIndex; monthIndex >= 0; monthIndex--) {
                int month = monthValues.get(monthIndex);
                mdt.setMonthOfYear(month);
                List<Integer> dayValues = parseDayValueList(fixedCronExp, mdt.toDateTime());
                int dayStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear())
                        ? searchNotGreaterThanIndex(dayValues, dateTime.getDayOfMonth()) : dayValues.size() - 1;

                for (int dayIndex = dayStartIndex; dayIndex >= 0; dayIndex--) {
                    int day = dayValues.get(dayIndex);
                    int maxDayOfMonth = mdt.toDateTime().dayOfMonth().withMaximumValue().toLocalDate().getDayOfMonth();
                    if (day > maxDayOfMonth) {
                        break;
                    }
                    mdt.setDayOfMonth(day);
                    int hourStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear() && day == dateTime.getDayOfMonth())
                            ? searchNotGreaterThanIndex(hourValues, dateTime.getHourOfDay()) : hourValues.size() - 1;

                    for (int hourIndex = hourStartIndex; hourIndex >= 0; hourIndex--) {
                        int hour = hourValues.get(hourIndex);
                        mdt.setHourOfDay(hour);
                        int minuteStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear() && day == dateTime.getDayOfMonth()
                                && hour == dateTime.getHourOfDay()) ? searchNotGreaterThanIndex(minuteValues, dateTime.getMinuteOfHour())
                                        : minuteValues.size() - 1;

                        for (int minuteIndex = minuteStartIndex; minuteIndex >= 0; minuteIndex--) {
                            int minute = minuteValues.get(minuteIndex);
                            mdt.setMinuteOfHour(minute);
                            int secondStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear()
                                    && day == dateTime.getDayOfMonth() && hour == dateTime.getHourOfDay() && minute == dateTime.getMinuteOfHour())
                                            ? searchNotGreaterThanIndex(secondValues, dateTime.getSecondOfMinute()) : secondValues.size() - 1;

                            for (int secondIndex = secondStartIndex; secondIndex >= 0; secondIndex--) {
                                int second = secondValues.get(secondIndex);
                                mdt.setSecondOfMinute(second);
                                if (mdt.isBefore(dateTime)) {
                                    return mdt.toDateTime();
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public List<DateTime> getTimeAfter(DateTime dateTime, int n) throws ParseException {
        if (n < 1) {
            throw new IllegalArgumentException("n should be > 0, but given " + n);
        }

        List<DateTime> list = null;
        MutableDateTime mdt = dateTime.toMutableDateTime();
        for (int i = 0; i < n; i++) {
            DateTime value = getTimeAfter(mdt.toDateTime());
            if (value != null) {
                if (list == null) {
                    list = new ArrayList<DateTime>();
                }
                list.add(value);
                mdt.setMillis(value.getMillis());
            } else {
                break;
            }
        }

        return list;
    }

    public List<DateTime> getTimeBefore(DateTime dateTime, int n) throws ParseException {
        if (n < 1) {
            throw new IllegalArgumentException("n should be > 0, but given " + n);
        }

        List<DateTime> list = null;
        MutableDateTime mdt = dateTime.toMutableDateTime();
        for (int i = 0; i < n; i++) {
            DateTime value = getTimeBefore(mdt.toDateTime());
            if (value != null) {
                if (list == null) {
                    list = new ArrayList<DateTime>();
                }
                list.add(value);
                mdt.setMillis(value.getMillis());
            } else {
                break;
            }
        }

        return list;
    }

    public boolean isValid() {
        DateTime dateTime = new DateTime(1970, 1, 1, 0, 0, 0);
        try {
            getTimeAfter(dateTime);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cronExp == null) ? 0 : cronExp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        CronExpression other = (CronExpression) obj;
        if (cronExp == null) {
            if (other.cronExp != null) {
                return false;
            }
        } else if (!cronExp.equals(other.cronExp)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return cronExp;
    }
}
