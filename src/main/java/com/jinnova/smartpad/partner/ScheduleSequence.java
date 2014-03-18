package com.jinnova.smartpad.partner;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Linux style schedule
 * 
 * Examples:
 * 
 * - Every monday in March/April 2014: dayInWeek = 0, month = {2, 3}, year = 2014
 * - Every day 8AM to 10PM, Sat/sun 9AM to 12PM:
 * 			hour = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21}, dayInWeek={0, 1, 2, 3, 4}
 * 			hour = {9, 10, 11}, dayInWeek={5, 6}
 * 
 * Numbers follow java.util.Calendar fields
 *
 */
public class ScheduleSequence {
	
	/**
	 * all arrays are sorted 
	 */
	private int[] minutes;
	
	private int[] hours;
	
	private int[] daysOfWeek;
	
	private int[] daysOfMonth;
	
	private int[] months;
	
	private int[] years;

	public ScheduleSequence(int[] minutes, int[] hours, int[] daysOfWeek,
			int[] daysOfMonth, int[] months, int[] years) {

		sort(minutes);
		this.minutes = minutes;
		
		sort(hours);
		this.hours = hours;
		
		sort(daysOfWeek);
		this.daysOfWeek = daysOfWeek;
		
		sort(daysOfMonth);
		this.daysOfMonth = daysOfMonth;
		
		sort(months);
		this.months = months;
		
		sort(years);
		this.years = years;
	}
	
	private static void sort(int[] source) {
		if (source == null) {
			return;
		}
		Arrays.sort(source);
	}

	public boolean isInAffect(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		if (!isInAffect(years, cal.get(Calendar.YEAR))) {
			return false;
		}
		if (!isInAffect(months, cal.get(Calendar.MONTH))) {
			return false;
		}
		if (!isInAffect(daysOfMonth, cal.get(Calendar.DAY_OF_MONTH))) {
			return false;
		}
		if (!isInAffect(daysOfWeek, cal.get(Calendar.DAY_OF_WEEK))) {
			return false;
		}
		if (!isInAffect(hours, cal.get(Calendar.HOUR_OF_DAY))) {
			return false;
		}
		if (!isInAffect(minutes, cal.get(Calendar.MINUTE))) {
			return false;
		}
		if (!isInAffect(years, cal.get(Calendar.YEAR))) {
			return false;
		}
		return true;
	}

	private static boolean isInAffect(int[] array, int value) {
		if (array == null) {
			return true;
		}
		
		return Arrays.binarySearch(array, value) >= 0;
	}
}
