package com.jinnova.smartpad.partner;

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class ScheduleSequenceTest extends TestCase {

	public void testIsInAffect() {
		ScheduleSequence schedule = ScheduleSequence.create(null, null, null, null, null, null);
		GregorianCalendar cal = new GregorianCalendar();
		assertTrue(schedule.isInAffect(cal.getTime()));
		
		//8AM to 5PM
		ScheduleSequence schedule8am5pm = ScheduleSequence.create(
				null, new int[] {8, 9, 10, 11, 12, 13, 14, 15, 16}, null, null, null, null);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		System.out.println(cal.getTime());
		assertTrue(schedule8am5pm.isInAffect(cal.getTime()));

		cal.set(Calendar.HOUR_OF_DAY, 6);
		System.out.println(cal.getTime());
		assertFalse(schedule8am5pm.isInAffect(cal.getTime()));
		
		//in March 2014
		ScheduleSequence march2014 = ScheduleSequence.create(
				null, null, null, null, new int[] {2}, new int[] {2014});
		cal.set(Calendar.YEAR, 2014);
		cal.set(Calendar.MONTH, 2);
		System.out.println(cal.getTime());
		assertTrue(march2014.isInAffect(cal.getTime()));

		cal.set(Calendar.MONTH, 1);
		System.out.println(cal.getTime());
		assertFalse(march2014.isInAffect(cal.getTime()));
	}

}
