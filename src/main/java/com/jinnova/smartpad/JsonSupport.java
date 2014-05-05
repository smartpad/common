package com.jinnova.smartpad;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonSupport {

	public static String getAsString(JsonObject json, String property) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(property);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsString();
	}

	public static JsonArray getAsJsonArray(JsonObject json, String property) {
		if (json == null) {
			return null;
		}
		JsonElement e = json.get(property);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsJsonArray();
	}

	public static JsonArray parseJsonArray(JsonParser parser, String json) {
		if (json == null) {
			return null;
		}
		return parser.parse(json).getAsJsonArray();
	}

	public static JsonObject parseJsonObject(JsonParser parser, String json) {
		if (json == null) {
			return null;
		}
		return parser.parse(json).getAsJsonObject();
	}
	
	public static JsonObject toJson(HashMap<String, String> map) {
		if (map == null) {
			return null;
		}
		
		JsonObject json = new JsonObject();
		for (Entry<String, String> entry : map.entrySet()) {
			json.addProperty(entry.getKey(), entry.getValue());
		}
		return json;
	}
	
	public static HashMap<String, String> toHashmap(JsonObject json) {
		if (json == null || json.isJsonNull()) {
			return null;
		}
		
		HashMap<String, String> map = new HashMap<>();
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			JsonElement e = entry.getValue();
			if (e.isJsonPrimitive()) {
				map.put(entry.getKey(), e.getAsString());
			} else {
				map.put(entry.getKey(), e.toString());
			}
		}
		return map;
	}
}
