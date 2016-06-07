package com.nzion.util;

import org.joda.time.LocalDateTime;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Mohan Sharma on 6/12/2015.
 */

public class CronExpressionGenerator {

    private static  String generateCronExpressionWhen(final String seconds, final String minutes, final String hours, final String dayOfMonth, final String month, final String dayOfWeek, final String year) {
        return String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s" , seconds, minutes, hours, dayOfMonth, month, dayOfWeek, year);
    }

    public static String generateCronExpressionGivenDateParameters(boolean isAfter, Date date, int hour){
        LocalDateTime localDate = new LocalDateTime(date);
        if(isAfter)
            localDate = localDate.plusMinutes(hour);
        else
            localDate = localDate.minusMinutes(hour);
        return CronExpressionGenerator.generateCronExpressionWhen(String.valueOf(localDate.getSecondOfMinute()), String.valueOf(localDate.getMinuteOfHour()), String.valueOf(localDate.getHourOfDay()), String.valueOf(localDate.getDayOfMonth()), String.valueOf(localDate.getMonthOfYear()), "?", String.valueOf(localDate.getYear()));
    }

    /*public static void main(String[] args) throws ParseException {
        System.out.println(generateCronExpressionGivenDateParameters(true, new Date(), 2));
        System.out.println(CronExpression.isValidExpression(generateCronExpressionGivenDateParameters(true, new Date(), 2)));
    }*/
}
