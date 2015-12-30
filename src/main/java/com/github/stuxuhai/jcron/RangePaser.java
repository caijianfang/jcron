/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import com.google.common.collect.Range;

public class RangePaser extends AbstractPaser {

    private Set<Integer> set;
    private Set<Integer> result;
    private Range<Integer> range;
    private DurationField type;
    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)-(\\d+)");

    protected RangePaser(Range<Integer> range, DurationField type) {
        super(range, type);
        this.range = range;
        this.type = type;
    }

    @Override
    protected boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = RANGE_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            int from = Integer.parseInt(m.group(1));
            int to = Integer.parseInt(m.group(2));
            if (from <= to && range.contains(from) && range.contains(to)) {
                if (set == null) {
                    set = new HashSet<Integer>();
                }

                for (int i = from; i <= to; i++) {
                    set.add(i);
                }

                return true;
            } else {
                throw new ParseException(
                        String.format("Invalid value of %s: %s, out of range %s", type.name, cronFieldExp, range.toString().replace("‥", ", ")), -1);
            }
        }

        return false;
    }

    @Override
    protected Set<Integer> parse(DateTime dateTime) {
        if (type.equals(DurationField.DAY_OF_WEEK)) {
            if (set != null) {
                if (result == null) {
                    result = new HashSet<Integer>();
                }

                result.clear();

                MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
                int maxDayOfMonth = mdt.getDayOfMonth();
                for (int i = 1; i <= maxDayOfMonth; i++) {
                    mdt.setDayOfMonth(i);
                    if (set.contains(mdt.getDayOfWeek())) {
                        result.add(mdt.getDayOfMonth());
                    }
                }

                return result;
            }
        }

        return set;
    }

}
