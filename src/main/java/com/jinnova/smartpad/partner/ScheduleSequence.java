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
	
	private final LinkedList<Integer> daysOfWeek = new LinkedList<Integer>();
	
	private final LinkedList<Integer> daysOfMonth = new LinkedList<Integer>();
	
	private final LinkedList<Integer> months = new LinkedList<Integer>();
	
	private final LinkedList<Integer> years = new LinkedList<Integer>();
	
	private static void addAll(LinkedList<Integer> dest, int[] source) {
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
		if (!years.contains(cal.get(Calendar.YEAR))) {
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
		return true;
	}
	
	/*public Date getEarliestStart() {

		Collections.sort(years);
		Collections.sort(months);
		Collections.sort(daysOfMonth);
		Collections.sort(daysOfWeek);
		Collections.sort(hours);
		Collections.sort(minutes);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(0);
		cal.set(Calendar.MINUTE, minutes.getFirst());
		return null;
	}
	
	public Date getLatestEnd() {

		Collections.sort(years);
		Collections.sort(months);
		Collections.sort(daysOfMonth);
		Collections.sort(daysOfWeek);
		Collections.sort(hours);
		Collections.sort(minutes);
		return null;
	}*/
	
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
