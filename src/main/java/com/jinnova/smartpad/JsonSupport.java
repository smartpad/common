package com.jinnova.smartpad;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
	
	@SuppressWarnings("unchecked")
	public static JsonObject toJson(HashMap<String, ?> map) {
		if (map == null) {
			return null;
		}
		
		JsonObject json = new JsonObject();
		for (Entry<String, ?> entry : map.entrySet()) {
			Object v = entry.getValue();
			if (v instanceof String) {
				json.addProperty(entry.getKey(), (String) v);
			} else if (v instanceof Map<?, ?>) {
				json.add(entry.getKey(), toJson((HashMap<String, ?>) v));
			} else {
				json.add(entry.getKey(), (JsonElement) v);
			}
		}
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> HashMap<String, T> toHashmap(JsonObject json) {
		if (json == null || json.isJsonNull()) {
			return null;
		}
		
		HashMap<String, T> map = new HashMap<>();
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			JsonElement e = entry.getValue();
			if (e.isJsonPrimitive()) {
				map.put(entry.getKey(), (T) e.getAsString());
			} else if (e.isJsonObject()) {
				map.put(entry.getKey(), (T) toHashmap(e.getAsJsonObject()));
			} else {
				map.put(entry.getKey(), (T) e);
			}
		}
		return map;
	}
	
	public static JsonArray toJsonArray(int[] array) {
		if (array == null) {
			return null;
		}
		
		JsonArray ja = new JsonArray();
		for (int n : array) {
			ja.add(new JsonPrimitive(String.valueOf(n)));
		}
		return ja;
	}
	
	public static JsonArray toJsonArray(String[] array) {
		if (array == null) {
			return null;
		}
		
		JsonArray ja = new JsonArray();
		for (String s : array) {
			ja.add(new JsonPrimitive(s));
		}
		return ja;
	}

	public static int[] toArray(JsonObject json, String memberName) {
		if (json == null) {
			return null;
		}
		JsonElement je = json.get(memberName);
		if (je == null || je.isJsonNull()) {
			return null;
		}
		JsonArray ja = je.getAsJsonArray();
		int[] array = new int[ja.size()];
		for (int i = 0; i < ja.size(); i++) {
			array[i] = ja.get(i).getAsInt();
		}
		return array;
	}
}
