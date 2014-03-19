package com.jinnova.smartpad.partner;

import java.util.Date;
import java.util.LinkedList;

public class Schedule implements ISchedule {

	private final LinkedList<IScheduleSequence> scheduleSequences = new LinkedList<IScheduleSequence>();
	
	private String text;
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public LinkedList<IScheduleSequence> getScheduleSequences() {
		return scheduleSequences;
	}
	
	public boolean isInAffect(Date date) {
		for (IScheduleSequence ss : scheduleSequences) {
			if (((ScheduleSequence) ss).isInAffect(date)) {
				return true;
			}
		}
		return false;
	}
}
