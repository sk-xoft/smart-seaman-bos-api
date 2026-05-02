package com.seaman.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class DateUtil {

    public static final Locale DEFAULT_LOCALE = new Locale("en", "EN");
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String YEAR_MONTH_DATE =  "yyyy-MM-dd";
    public static final String DATE_MONTH_YEAR =  "dd-MM-yyyy";
    public static final String DATE_OF_BIRTH = "dd-MMM-yyyy";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYY_MM = "yyyy/MM";

    public boolean checkPassDateRemainTime(Date expireDate, int remainTimeSec){
        boolean status = true;
        Date currentDate = new Date();
        if(expireDate.before(currentDate) ){
            status = false;
        }
        long dateDiff = expireDate.getTime() - currentDate.getTime();
        long dateDiffSeconds = TimeUnit.MILLISECONDS.toSeconds(dateDiff);

        if(remainTimeSec > 0 && dateDiffSeconds < remainTimeSec){
            status =  false;
        }

        return status;
    }

    public Date parseStringToDate(String dateString, String pattern) throws ParseException {
        SimpleDateFormat format;
        if (StringUtils.isNotEmpty(dateString) && StringUtils.isNotEmpty(pattern)) {
            format = new SimpleDateFormat(pattern, DEFAULT_LOCALE);
            return format.parse(dateString);
        } else {
            return null;
        }
    }

    public Date convertStringToDate(String dateStr, String dateFormatStr) {
        if (dateStr == null) {
            return null;
        } else {
            try {
                return (new SimpleDateFormat(dateFormatStr)).parse(dateStr);
            } catch (ParseException var3) {
                return null;
            }
        }
    }

    public DateFormat dateFormatToBc(String pattern) {
        Locale locale =  Locale.forLanguageTag("th-TH");
        return new SimpleDateFormat(pattern, locale);
    }

    public String formatDateToStr(String dateStr, String pattern){
        return dateFormatToBc(YYYYMMDD).format(convertStringToDate(dateStr,pattern));
    }

    public String formatDateToXDISReport(String dateStr, String pattern) {
        return dateFormatToBc(YYYY_MM).format(convertStringToDate(dateStr,pattern));
    }

    public String formatDateToString(Date date, String pattern) {
        SimpleDateFormat format;
        if (date != null && StringUtils.isNotEmpty(pattern)) {
            format = new SimpleDateFormat(pattern, DEFAULT_LOCALE);
            return format.format(date);
        } else {
            return null;
        }
    }

    public boolean validateDate(String dateString, String pattern) {
        SimpleDateFormat format;
        boolean isDateValid = false;

        try {
            if (StringUtils.isNotEmpty(dateString) && StringUtils.isNotEmpty(pattern)) {
                format = new SimpleDateFormat(pattern, DEFAULT_LOCALE);
                format.parse(dateString);
                isDateValid = true;
            }
        } catch (ParseException e) {
            isDateValid = false;
        }
        return isDateValid;
    }

    public String convertIntToDate(Integer intDate) {

        int intYear = intDate/100;
        int intMonth = intDate - (intYear * 100);

        Calendar result = new GregorianCalendar();
        result.set(intYear, intMonth - 1, 1, 0, 0, 0);

        return formatDateToString(result.getTime(), DATE_TIME);
    }

    public long datetimeDiffSecond(Date start, Date end) {
        long diffInMillies = Math.abs(start.getTime() - end.getTime());
        return TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public Date datetimeDiffSecond(int sec) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());// all done

        cal.add(Calendar.SECOND, -sec);
        return cal.getTime();
    }

    public String diffDate(int date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());// all done
        cal.add(Calendar.DATE, -date);
        return formatDateToString(cal.getTime(), YYYYMMDD);
    }

    public String dateSendToDopa(String dateOfBirth){

        String[] strDateArr = dateOfBirth.split("/");

        Integer yearInt =  Integer.valueOf(strDateArr[2]);
        yearInt = yearInt +  543;

        Integer monthInt = Integer.valueOf(strDateArr[1]);
        Integer dayInt = Integer.valueOf(strDateArr[0]);

        return String.format("%04d", yearInt) + String.format("%02d", monthInt)  + String.format("%02d", dayInt);
    }

    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat(DATE_TIME);
        return format.format(date);
    }

    public String getCurrentDateTime(){
        long millis = new Date().getTime();
        return this.convertTime(millis);
    }

    public int getCurrentYear() {
        return  Calendar.getInstance().get(Calendar.YEAR);
    }

    public Period calculateDisplayAge(String dateOfBirth) {
        Date certEndDate = convertStringToDate(dateOfBirth, DateUtil.YEAR_MONTH_DATE);
        Calendar certEndDateCal = dateToCalendar(certEndDate);
        LocalDate endDate = LocalDate.of(certEndDateCal.get(Calendar.YEAR),certEndDateCal.get(Calendar.MONTH) + 1, certEndDateCal.get(Calendar.DATE));
        LocalDate today = LocalDate.now();
        return Period.between(endDate, today);
    }

    public Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public Period calculateDisplayDateCertRemain(String certEndDateStr) {
        Date certEndDate = convertStringToDate(certEndDateStr, DateUtil.YEAR_MONTH_DATE);
        Calendar certEndDateCal = dateToCalendar(certEndDate);
        LocalDate endDate = LocalDate.of(certEndDateCal.get(Calendar.YEAR),certEndDateCal.get(Calendar.MONTH) + 1, certEndDateCal.get(Calendar.DATE));
        LocalDate today = LocalDate.now();
        return Period.between(today, endDate);
    }

}
