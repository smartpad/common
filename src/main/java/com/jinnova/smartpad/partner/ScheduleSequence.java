package com.jinnova.smartpad.partner;

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
 */
public class ScheduleSequence {
	
	private int[] minute;
	
	private int[] hour;
	
	private int[] dayInWeek;
	
	private int[] dayInMonth;
	
	private int[] month;
	
	private int[] year;

}
