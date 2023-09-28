package com.conpo.audiorac.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private static final String LOG_TAG = "DateUtil";

    public static long dateDiff(Calendar cal1, Calendar cal2, String flag) {

        long milliseconds1 = cal1.getTimeInMillis();
        long milliseconds2 = cal2.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;

        if (flag.equals("S")) {
            return diff;
        }
        else if (flag.equals("s")) {
            long diffSeconds = diff / 1000;
            return diffSeconds;
        }
        else if (flag.equals("m")) {
            long diffMinutes = diff / (60 * 1000);
            return diffMinutes;
        }
        else if (flag.equals("H")) {
            long diffHours = diff / (60 * 60 * 1000);
            return diffHours;
        }
        else if (flag.equals("d")) {
            long diffDays = diff / (24 * 60 * 60 * 1000);
            return diffDays;
        }

        return 0;
    }

    public static long getBetweenDay(Calendar before, Calendar after) {

        Calendar before_cal = before;
        Calendar after_cal = after;
        int count = 0;


        if (before.equals(after) || before == after) {
            return 0;
        }
        else if (before_cal.before(after_cal)) {

            while (before_cal.before( after_cal ))  {
                before_cal.add( Calendar.DATE, 1 );
                if (before_cal.after( after_cal ) ) {
                    break;
                }

                count++;
            }

        }
        else if (before_cal.after(after_cal)) {
            while (before_cal.after( after_cal ) ) {

                before_cal.add ( Calendar.DATE, -1 );
                if (before_cal.before( after_cal ) ) {
                    break;
                }

                count--;
            }
        }

        return count;
    }

    // "yyyy/MM/dd HH:mm:ss/SSS"
    public static Calendar getCalendar(String format, String date) throws ParseException {
        Date dateobj = getDate(format, date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateobj);

        return cal;
    }

    public static Calendar getCalendar(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c;
    }
    public static Calendar getCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        return cal;
    }

    public static Date getDate(String format, String date) throws ParseException {
        SimpleDateFormat sd = new SimpleDateFormat(format, Locale.KOREA);
        Date dateobj = sd.parse(date);

        return dateobj;
    }

    /**
     * 현재 날짜 구하기 ( "yyyy/MM/dd HH:mm:ss/SSS" )
     * @param format
     * @return
     */
    public static String getDate(String format) {
        if (format == null)
            format = "yyyy/MM/dd HH:mm:ss/SSS";

        return dateFormat(new Date(), format);
    }

    // 전날짜 구하기
    public static String getDiffDate(String format, int daysDiff) {
        Calendar cal = getCalendar();
        cal.add(Calendar.DATE, daysDiff);

        return dateFormat(cal, format);
    }

    public static String dateFormat(Calendar calendar, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat( format, Locale.KOREA );
        String dTime = formatter.format( calendar.getTime() );
        return dTime;
    }

    public static String dateFormat(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat( format, Locale.KOREA );
        String dTime = formatter.format( date );
        return dTime;
    }

    /**
     * '20130509192052' 형식의 문자열을 'yyyy/MM/dd HH:mm:ss/SSS'와 같은 포맷으로 만들어 준다
     * @param dateStr
     * @param format
     * @return
     */
    public static String dateFormat(String dateStr, String format) {
        if (dateStr.indexOf("-") >= 0)
            dateStr = dateStr.replace("-", "");

        if (dateStr.indexOf("/") >= 0)
            dateStr = dateStr.replace("/", "");

        if (dateStr.indexOf(".") >= 0)
            dateStr = dateStr.replace(".", "");

        if (dateStr.indexOf("T") >= 0)
            dateStr = dateStr.replace("T", "");

        dateStr = dateStr.replace(":", "");
        dateStr = dateStr.replace(" ", "");

        if (dateStr.length() < 8)
            return dateStr;
        else if (dateStr.length() == 8)
            dateStr += "000000";

        try {
            SimpleDateFormat formatter = new SimpleDateFormat( format, Locale.KOREA );
            String dTime = formatter.format( getDate( "yyyyMMddHHmmss", dateStr.substring(0, 14)) );
            return dTime;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error on dateFormat() : " + e.getMessage());
            return "";
        }
    }

}
