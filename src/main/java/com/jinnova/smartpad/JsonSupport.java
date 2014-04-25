package com.jinnova.smartpad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonSupport {

	public static String getAsString(JsonObject json, String property) {
		JsonElement e = json.get(property);
		if (e == null || e.isJsonNull()) {
			return null;
		}
		return e.getAsString();
	}
}
