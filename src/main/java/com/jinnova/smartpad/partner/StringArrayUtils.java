package com.jinnova.smartpad.partner;

import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

	public static <T> void load(Collection<T> list, T[] array) {
		list.clear();
		for (T t : array) {
			list.add(t);
		}
	}
	
	public static int compare(ICatalogItem item1, ICatalogItem item2, String fieldId) {
		return compare(item1.getFieldValue(fieldId), item2.getFieldValue(fieldId));
	}
	
	public static int compare(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return 0;
		}
		if (s1 == null) {
			return -1;
		}
		if (s2 == null) {
			return 1;
		}
		return s1.compareTo(s2);
	}
	
	public static String getAsString(JsonObject json, String name) {
		JsonElement element = json.get(name);
		if (element == null || element.isJsonNull()) {
			return null;
		}
		return element.getAsString();
	}
}
