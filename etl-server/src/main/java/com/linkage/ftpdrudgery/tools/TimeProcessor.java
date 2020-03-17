package com.linkage.ftpdrudgery.tools;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间通用类
 * @author linkage
 *
 */
public class TimeProcessor {
    /**
     * 默认构造器
     */
    public TimeProcessor() {
    }
    
    /**
     * long 转 String(yyyyMMddHHmmss)
     * @param longTime
     * @return
     */
    public static String getDateStringFromLong(long longTime){
    	java.util.Date date = new java.util.Date(longTime);
    	return date2String(date, "yyyyMMddHHmmss");
    }
    /**
     * 获得系统当前时间串,时间格式为yyyyMMddHHmmss [20041120203020]
     * @return String
     */
    public static String getCurrentTime() {
        java.util.Date date = new java.util.Date();
        return date2String(date, "yyyyMMddHHmmss");
    }
    
    /**
     * 获得系统当前时间
     * @return Date
     */
    public static Date getCurrentTimeDate() {
        java.util.Date date = new java.util.Date();
        return date;
    }
    
    /**
     * 返回String 格式为当前时间的yyyyMMdd形式
     * @return
     */
    public static String getCurrentTimeString() {
        java.util.Date date = new java.util.Date();
        return date2String(date, "yyyyMMdd");
    }
    
    /**
     * 得到昨天的日志
     * @return
     */
    public static String getYesterdayDate() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		Date today = new Date();
		today.setTime(today.getTime() - 60 * 24 * 60 * 1000);
		return sf.format(today).toString();
	}

    /**
     * 使用制定的格式获取当前的系统时间串
     * @param format String 制定的时间格式
     * @return String 返回的时间
     */
    public static String getCurrentTime(String format) {
        java.util.Date date = new java.util.Date();
        return date2String(date, format);
    }

    /**
     * 把时间串按照响应的格式转换成日期对象
     * @param dateStr 时间串
     * @param format 指定的格式
     * @return 返回java.util.Date的对象,转换失败时返回当前的时间对象
     */
    public static java.util.Date string2Date(String dateStr, String format) throws
        Exception {
        if (dateStr == null || format == null)
        {
            {
                throw new Exception("日期转换失败:dateStr or format is null");
            }

        }
        try
        {
            SimpleDateFormat dateFormater = new SimpleDateFormat(format);
            return dateFormater.parse(dateStr);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /**
     * 使用指定的格式格式当前的日期对象
     * @param obj Date 要格式化的时间对象 为空时返回但前时间
     * @param format String 指定的格式
     * @return String 返回的时间串
     */
    public static String date2String(java.util.Date obj, String format) {
        if (obj == null)
        {
            obj = new java.util.Date();
        } //yyyyMMddHHmmss
        SimpleDateFormat dateFormater = new SimpleDateFormat(format);
        return dateFormater.format(obj);
    }

    /**
     * 把util的Date类型转换成java.sql.Timestam
     * @param date Date
     * @return Timestamp
     */
    public static Timestamp utilDate2Timestamp(java.util.Date date) {
        if (date == null)
        {
            date = new java.util.Date();
        }
        return new java.sql.Timestamp(date.getTime());
    }

    /**
     * 得到当前系统时间戳
     * @return java.sql.Timestamp
     */
    public static Timestamp getCurrentTimestamp() {
        return new java.sql.Timestamp(new java.util.Date().getTime());
    }

    /**
     * 得到当前时间的java.sql.Date对象
     * @return
     */
    public static java.sql.Date getCurrentSQLDate() {
        return new java.sql.Date(new java.util.Date().getTime());
    }

    /**
     * 将指定的util时间对象转换成java.sql.Date
     * @param date Date
     * @return Date
     */
    public static java.sql.Date utilDate2SQLDate(java.util.Date date) {
        if (date == null)
        {
            return getCurrentSQLDate();
        }
        return new java.sql.Date(date.getTime());
    }

    /**
     * 将字符串的时间值转换成java.sql.Timestamp类型
     * @param dateStr String 如果dateStr长度为8转换yyyyMMdd,如果长度为14转换为yyyyMMddHHmmss
     * @return Date
     */
    public static java.sql.Timestamp string2SQLDate(String dateStr) throws
        Exception {

        String format = null;
        if (dateStr.length() == 8)
        {
            format = "yyyyMMdd";
        }
        else if (dateStr.length() == 14)
        {
            format = "yyyyMMddHHmmss";
        }

        return string2SQLDate(dateStr, format);
    }

    /**
     * 将字符串的时间值按照指定的格式转换成java.sql.Timestamp类型
     * @param dateStr String
     * @param format String
     * @return Timestamp
     * @throws Exception
     */
    public static java.sql.Timestamp string2SQLDate(String dateStr,
                                                    String format) throws
        Exception {
        if (dateStr == null)
        {
            throw new Exception("日期转换失败:dateStr is null");
        }
        java.util.Date date = null;
        java.sql.Timestamp timestamp = null;
        try
        {
            date = string2Date(dateStr, format);
            timestamp = utilDate2Timestamp(date);
        }
        catch (Exception ex)
        {
            throw ex;
        }
        return timestamp;
    }

    public static String convertDateForm(String dateStr, String origFormat, String targetFormat) throws Exception{
        SimpleDateFormat origFormater = new SimpleDateFormat(origFormat);
        Date date = origFormater.parse(dateStr);
        SimpleDateFormat targetFormater = new SimpleDateFormat(targetFormat);
        return targetFormater.format(date);
    }
    
    public static void main(String[] args) {
    	System.out.println(TimeProcessor.getCurrentTime("MM"));
    	System.out.println(getDateStringFromLong(1320521217203l));
    }
}
