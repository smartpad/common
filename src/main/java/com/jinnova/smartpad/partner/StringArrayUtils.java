package com.jinnova.smartpad.partner;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class StringArrayUtils {
	
	public static String stringArrayToJson(String[] array) {
		JsonArray ja = new JsonArray();
		for (String s : array) {
			ja.add(new JsonPrimitive(s));
		}
		return ja.toString();
	}
	
	public static String[] stringArrayFromJson(String s) {
		JsonParser p = new JsonParser();
		JsonArray ja = p.parse(s).getAsJsonArray();
		String[] array = new String[ja.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = ja.get(i).getAsString();
		}
		return array;
	}

}
