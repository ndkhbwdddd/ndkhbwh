package com.yitianyike.calendar.appserver.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat timeFormat = new SimpleDateFormat("hhmmss");

	public static String getCurrentDate() {
		return dateFormat.format(Calendar.getInstance().getTime());
	}

	public static String getCurrentTime() {
		return timeFormat.format(Calendar.getInstance().getTime());
	}

	public static String getDecreaseDate(String befDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String decreaseDate = null;
		try {
			Date parse = sdf.parse(befDate);
			Calendar date = Calendar.getInstance();
			date.setTime(parse);
			date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
			decreaseDate = sdf.format(date.getTime());
		} catch (ParseException e) {
			return decreaseDate;
		}
		return decreaseDate;
	}

	public static String getGMTTime() {
		Calendar cd = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
		String str = sdf.format(cd.getTime());
		return str;
	}

	public static void main(String[] args) {
		System.out.println(getCurrentDate());
		System.out.println(getCurrentTime());
	}
}
