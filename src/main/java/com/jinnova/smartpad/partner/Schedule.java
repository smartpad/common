package com.jinnova.smartpad.partner;

import java.util.Date;
import java.util.LinkedList;

public class Schedule {

	private LinkedList<ScheduleSequence> scheduleSequences;
	
	private String text;
	
	public boolean isInAffect(Date date) {
		for (ScheduleSequence ss : scheduleSequences) {
			if (ss.isInAffect(date)) {
				return true;
			}
		}
		return false;
	}
}
