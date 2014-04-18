package com.jinnova.smartpad.drilling;

import java.util.ArrayList;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.partner.IDetailManager;

class DrillResult {
	
	private ArrayList<DrillSection> allSections = new ArrayList<>(10);
	
	private interface DrillSection {
		
		boolean copyTo(LinkedList<JsonObject> jsonList);
		
		JsonObject getJson();
	}
	
	private class SimpleSection implements DrillSection {
		
		String sectionType;
		Object[] ja;
		int expectedSize;
		
		SimpleSection(String sectionType, Object[] ja, int expectedSize) {
			this.sectionType = sectionType;
			this.ja = ja;
			this.expectedSize = expectedSize;
		}
		
		@Override
		public JsonObject getJson() {
			if (ja == null || ja.length == 0) {
				return null;
			}
			if (ja.length == 1) {
				return ((Feed) ja[0]).generateFeedJson();
			}
			
			//trim off if less expected count
			if (expectedSize < ja.length) {
				Object[] jaTemp = new Object[expectedSize];
				for (int i = 0; i < expectedSize; i++) {
					jaTemp[i] = ja[i];
				}
				ja = jaTemp;
			}
			
			JsonArray array = new JsonArray();
			for (Object o : ja) {
				array.add(((Feed) o).generateFeedJson());
			}
			
			JsonObject json = new JsonObject();
			json.addProperty(IDetailManager.FIELD_TYPE, sectionType);
			json.add(IDetailManager.FIELD_ARRAY, array);
			return json;
		}
		
		boolean isEmpty() {
			return ja == null || ja.length == 0;
		}
		
		@Override
		public boolean copyTo(LinkedList<JsonObject> jsonList) {

			if (ja == null) {
				return false;
			}
			
			boolean copied = false;
			for (int i = 0; i < expectedSize; i++) {
				if (i >= ja.length) {
					break;
				}
				jsonList.add(((Feed) ja[i]).generateFeedJson());
				copied = true;
			}
			return copied;
		}
	}
	
	private class TwinSection implements DrillSection {
		
		String sectionType;
		SimpleSection section1, section2;
		
		TwinSection(String sectionType, SimpleSection section1, SimpleSection section2) {
			this.sectionType = sectionType;
			this.section1 = section1;
			this.section2 = section2;
		}
		
		@Override
		public boolean copyTo(LinkedList<JsonObject> jsonList) {
			
			//flatten if needed
			if (section1.isEmpty()) {
				section2.expectedSize = section2.ja.length;
				return section2.copyTo(jsonList);
			}
			if (section2.isEmpty()) {
				section1.expectedSize = section1.ja.length;
				return section1.copyTo(jsonList);
			}
			
			//adjust sizes
			if (section1.ja.length < section1.expectedSize) {
				section2.expectedSize += section1.expectedSize - section1.ja.length;
			} else if (section2.ja.length < section2.expectedSize) {
				section1.expectedSize += section2.expectedSize - section2.ja.length;
			}
			
			boolean copied = section1.copyTo(jsonList);
			copied = section2.copyTo(jsonList);
			return copied;
			
		}
		
		@Override
		public JsonObject getJson() {
			
			LinkedList<JsonObject> jsonList = new LinkedList<>();
			if (!copyTo(jsonList)) {
				return null;
			}
			
			if (jsonList.size() == 1) {
				//flatten
				return jsonList.get(0);
			}
			
			JsonObject json = new JsonObject();
			json.addProperty(IDetailManager.FIELD_TYPE, sectionType);
			
			JsonArray ja = new JsonArray();
			for (JsonObject o : jsonList) {
				ja.add(o);
			}
			json.add(IDetailManager.FIELD_ARRAY, ja);
			return json;
		}
	}
	
	void add(String sectionType, Object[] ja, int expectedSize) {
		allSections.add(new SimpleSection(sectionType, ja, expectedSize));
	}
	
	void add(String sectionType, Object[] ja1, int expectedSize1, Object[] ja2, int expectedSize2) {
		
		allSections.add(new TwinSection(sectionType, 
				new SimpleSection(sectionType, ja1, expectedSize1), 
				new SimpleSection(sectionType, ja2, expectedSize2)));
	}
	
	public JsonArray toJson() {
		
		boolean flatten = false;
		LinkedList<JsonObject> jsonList = new LinkedList<>();
		for (int i = allSections.size() - 1; i >= 0; i--) {
			DrillSection oneSection = allSections.get(i);
			if (!flatten) {
				flatten = oneSection.copyTo(jsonList);
			} else {
				JsonObject oneJson = oneSection.getJson();
				if (oneJson != null) {
					jsonList.add(oneJson);
				}
			}
		}
		
		JsonArray ja = new JsonArray();
		for (int i = jsonList.size() - 1; i >= 0; i--) {
			ja.add(jsonList.get(i));
		}
		
		return ja;
	}
}
