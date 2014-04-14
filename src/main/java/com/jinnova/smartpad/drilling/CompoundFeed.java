package com.jinnova.smartpad.drilling;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.partner.IDetailManager;

public class CompoundFeed {
	
	public static JsonObject generateFeedJson(String typeName, JsonArray ja) {
		JsonObject json = new JsonObject();
		json.addProperty("type", IDetailManager.TYPENAME_COMPOUND);
		json.add("a", ja);
		return json;
	}
}
