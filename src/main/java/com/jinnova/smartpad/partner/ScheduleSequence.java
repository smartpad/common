package com.jinnova.smartpad.partner;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
public class ScheduleSequence implements IScheduleSequence {
	
	/**
	 * all arrays are sorted 
	 */
	private final LinkedList<Integer> minutes = new LinkedList<Integer>();

	private final LinkedList<Integer> hours = new LinkedList<Integer>();

	/**
	 * either daysOfMonth or daysOfWeek can be used once
	 */
	private final LinkedList<Integer> daysOfWeek = new LinkedList<Integer>();
	
	/**
	 * either daysOfMonth or daysOfWeek can be used once
	 */
	private final LinkedList<Integer> daysOfMonth = new LinkedList<Integer>();
	
	private final LinkedList<Integer> months = new LinkedList<Integer>();
	
	private final LinkedList<Integer> years = new LinkedList<Integer>();
	
	private static void addAll(LinkedList<Integer> dest, int[] source) {
		if (source == null) {
			return;
		}
		for (int i : source) {
			dest.add(i);
		}
	}
	
	private static int[] toArray(LinkedList<Integer> source) {
		int[] array = new int[source.size()];
		int i = 0;
		for (int one : source) {
			array[i] = one;
			i++;
		}
		return array;
	}
	
	private static void fromArray(LinkedList<Integer> dest, int[] numbers) {
		dest.clear();
		for (int i : numbers) {
			dest.add(i);
		}
	}
	
	public static ScheduleSequence create(int[] minutes, int[] hours, int[] daysOfWeek,
			int[] daysOfMonth, int[] months, int[] years) {

		ScheduleSequence s = new ScheduleSequence();
		addAll(s.minutes, minutes);
		addAll(s.hours, hours);
		addAll(s.daysOfWeek, daysOfWeek);
		addAll(s.daysOfMonth, daysOfMonth);
		addAll(s.months, months);
		addAll(s.years, years);
		return s;
	}
	
	public int[] getMinutes() {
		return toArray(minutes);
	}

	public int[] getHours() {
		return toArray(hours);
	}

	public int[] getDaysOfWeek() {
		return toArray(daysOfWeek);
	}

	public int[] getDaysOfMonth() {
		return toArray(daysOfMonth);
	}

	public int[] getMonths() {
		return toArray(months);
	}

	public int[] getYears() {
		return toArray(years);
	}
	
	public void setMinutes(int[] numbers) {
		fromArray(minutes, numbers);
	}

	public void setHours(int[] numbers) {
		fromArray(hours, numbers);
	}

	public void setDaysOfWeek(int[] numbers) {
		fromArray(daysOfWeek, numbers);
	}

	public void setDaysOfMonth(int[] numbers) {
		fromArray(daysOfMonth, numbers);
	}

	public void setMonths(int[] numbers) {
		fromArray(months, numbers);
	}

	public void setYears(int[] numbers) {
		fromArray(years, numbers);
	}

	public boolean isInAffect(Date date) {

		Collections.sort(years);
		Collections.sort(months);
		Collections.sort(daysOfMonth);
		Collections.sort(daysOfWeek);
		Collections.sort(hours);
		Collections.sort(minutes);
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		
		return calendarFieldContainsValue(years, cal, Calendar.YEAR) &&
				calendarFieldContainsValue(months, cal, Calendar.MONTH) &&
				calendarFieldContainsValue(daysOfMonth, cal, Calendar.DAY_OF_MONTH) &&
				calendarFieldContainsValue(daysOfWeek, cal, Calendar.DAY_OF_WEEK) &&
				calendarFieldContainsValue(hours, cal, Calendar.HOUR_OF_DAY) &&
				calendarFieldContainsValue(minutes, cal, Calendar.MINUTE);
		
		/*if (!years.isEmpty() && !years.contains(cal.get(Calendar.YEAR))) {
			return false;
		}
		if (!calendarFieldContainsValue(years, cal, Calendar.YEAR)) {
			return false;
		}
		if (!months.contains(cal.get(Calendar.MONTH))) {
			return false;
		}
		if (!daysOfMonth.contains(cal.get(Calendar.DAY_OF_MONTH))) {
			return false;
		}
		if (!daysOfWeek.contains(cal.get(Calendar.DAY_OF_WEEK))) {
			return false;
		}
		if (!hours.contains(cal.get(Calendar.HOUR_OF_DAY))) {
			return false;
		}
		if (!minutes.contains(cal.get(Calendar.MINUTE))) {
			return false;
		}
		if (!years.contains(cal.get(Calendar.YEAR))) {
			return false;
		}
		return true;*/
	}
	
	private static boolean calendarFieldContainsValue(LinkedList<Integer> values, GregorianCalendar cal, int calField) {
		return values.isEmpty() || values.contains(cal.get(calField));
	}
	
	public Date getEarliestStart() {

		Collections.sort(years);
		Collections.sort(months);
		Collections.sort(daysOfMonth);
		Collections.sort(daysOfWeek);
		Collections.sort(hours);
		Collections.sort(minutes);
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(0);
		if (years.isEmpty()) {
			//infinite-start
			return null;
		} else {
			cal.set(Calendar.YEAR, years.getFirst());
		}
		setFirst(cal, Calendar.MONTH, months, Calendar.JANUARY);	
		if (daysOfWeek.isEmpty()) {
			setFirst(cal, Calendar.DAY_OF_MONTH, daysOfMonth, 1);	
		} else {
			cal.set(Calendar.DAY_OF_WEEK, daysOfWeek.getFirst());
		}
		setFirst(cal, Calendar.HOUR_OF_DAY, hours, 0);
		setLast(cal, Calendar.MINUTE, minutes, 0);
		
		return cal.getTime();
	}
	
	public Date getLatestEnd() {

		Collections.sort(years);
		Collections.sort(months);
		Collections.sort(daysOfMonth);
		Collections.sort(daysOfWeek);
		Collections.sort(hours);
		Collections.sort(minutes);
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(0);
		if (years.isEmpty()) {
			//never ends
			return null;
		}
		cal.set(Calendar.YEAR, years.getLast());
		setLast(cal, Calendar.MONTH, months, Calendar.DECEMBER);		
		if (daysOfWeek.isEmpty()) {
			setLast(cal, Calendar.DAY_OF_MONTH, daysOfMonth, cal.getLeastMaximum(Calendar.DAY_OF_MONTH));
		} else {
			cal.set(Calendar.DAY_OF_WEEK, daysOfWeek.getLast());
		}
		setLast(cal, Calendar.HOUR_OF_DAY, hours, 23);
		setLast(cal, Calendar.MINUTE, minutes, 59);
		
		return cal.getTime();
	}
	
	private static void setFirst(GregorianCalendar cal, int calField, LinkedList<Integer> values, int minimum) {
		if (values.isEmpty()) {
			cal.set(calField, minimum);
		} else {
			cal.set(calField, values.getFirst());
		}
	}
	
	private static void setLast(GregorianCalendar cal, int calField, LinkedList<Integer> values, int maximum) {
		if (values.isEmpty()) {
			cal.set(calField, maximum);
		} else {
			cal.set(calField, values.getLast());
		}
	}
	
	private static JsonArray toJson(LinkedList<Integer> numbers) {
		JsonArray ja = new JsonArray();
		for (Integer n : numbers) {
			ja.add(new JsonPrimitive(n));
		}
		return ja;
	}
	
	private static void fromJson(LinkedList<Integer> dest, JsonArray numbers) {
		for (int i = 0; i < numbers.size(); i++) {
			dest.add(numbers.get(i).getAsInt());
		}
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.add("m", toJson(this.minutes));
		json.add("h", toJson(this.hours));
		json.add("w", toJson(this.daysOfWeek));
		json.add("d", toJson(this.daysOfMonth));
		json.add("mo", toJson(this.months));
		json.add("y", toJson(this.years));
		return json;
	}
	
	public static ScheduleSequence fromJson(JsonObject json) {
		ScheduleSequence seq = new ScheduleSequence();
		fromJson(seq.minutes, json.get("m").getAsJsonArray());
		fromJson(seq.hours, json.get("h").getAsJsonArray());
		fromJson(seq.daysOfWeek, json.get("w").getAsJsonArray());
		fromJson(seq.daysOfMonth, json.get("d").getAsJsonArray());
		fromJson(seq.months, json.get("mo").getAsJsonArray());
		fromJson(seq.years, json.get("y").getAsJsonArray());
		return seq;
		
	}
}
