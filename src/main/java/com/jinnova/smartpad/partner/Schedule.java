package com.jinnova.smartpad.partner;

import java.util.Date;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class Schedule implements ISchedule {

	private final LinkedList<IScheduleSequence> scheduleSequences = new LinkedList<IScheduleSequence>();
	
	private String text;
	
	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public IScheduleSequence newScheduleSequenceInstance() {
		return new ScheduleSequence();
	}
	
	@Override
	public IScheduleSequence[] getScheduleSequences() {
		return scheduleSequences.toArray(new IScheduleSequence[scheduleSequences.size()]);
	}
	
	@Override
	public void setScheduleSequences(IScheduleSequence[] sequences) {
		scheduleSequences.clear();
		for (IScheduleSequence seq : sequences) {
			if (seq == null) {
				continue;
			}
			scheduleSequences.add(seq);
		}
	}
	
	public boolean isInAffect(Date date) {
		for (IScheduleSequence ss : scheduleSequences) {
			if (((ScheduleSequence) ss).isInAffect(date)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		JsonArray ja = new JsonArray();
		for (IScheduleSequence seq : this.scheduleSequences) {
			ja.add(((ScheduleSequence) seq).toJson());
		}
		return ja.toString();
	}
	
	public void fromString(String s) {
		if (s == null || "".equals(s)) {
			return;
		}
		JsonParser jp = new JsonParser();
		JsonArray ja = jp.parse(s).getAsJsonArray();
		for (int i = 0; i < ja.size(); i++) {
			scheduleSequences.add(ScheduleSequence.fromJson(ja.get(i).getAsJsonObject()));
		}
	}
}
