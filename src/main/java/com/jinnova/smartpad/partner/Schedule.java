package com.jinnova.smartpad.partner;

import java.util.Date;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.JsonSupport;

public class Schedule implements ISchedule {

	private final LinkedList<IScheduleSequence> scheduleSequences = new LinkedList<IScheduleSequence>();
	
	private String desc;
	
	@Override
	public String getDesc() {
		return desc;
	}

	@Override
	public void setDesc(String desc) {
		this.desc = desc;
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
	
	/*@Override
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
	}*/
	
	public JsonObject writeJson() {
		JsonObject json = new JsonObject();
		json.addProperty("desc", desc);
		JsonArray ja = new JsonArray();
		for (IScheduleSequence ss : scheduleSequences) {
			ja.add(((ScheduleSequence) ss).writeJson());
		}
		json.add("seqs", ja);
		return json;
	}
	
	public void readJson(JsonObject json) {
		desc = JsonSupport.getAsString(json, "desc");
		JsonArray ja = json.get("seqs").getAsJsonArray();
		if (ja == null) {
			return;
		}
		for (int i = 0; i < ja.size(); i++) {
			ScheduleSequence seq = new ScheduleSequence();
			seq.readJson(ja.get(i).getAsJsonObject());
			scheduleSequences.add(seq);
		}
		return;
	}
	
	public Date getEarliestStart() {
		
		Date earliest = null;
		for (IScheduleSequence ss : scheduleSequences) {
			Date d = ((ScheduleSequence) ss).getEarliestStart();
			if (d == null) {
				//started already
				return null;
			}
			if (earliest == null || earliest.after(d)) {
				earliest = d;
			}
		}
		return earliest;
	}
	
	public Date getLatestEnd() {
		
		Date latest = null;
		for (IScheduleSequence ss : scheduleSequences) {
			Date d = ((ScheduleSequence) ss).getLatestEnd();
			if (d == null) {
				//started already
				return null;
			}
			if (latest == null || latest.before(d)) {
				latest = d;
			}
		}
		return latest;
	}
}
