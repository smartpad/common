package com.jinnova.smartpad;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonSupport {

	public static String getAsString(JsonObject json, String property) {
		JsonElement e = json.get(property);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsString();
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
}
