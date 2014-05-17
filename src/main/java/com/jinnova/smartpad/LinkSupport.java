package com.jinnova.smartpad;

import static com.jinnova.smartpad.partner.IDetailManager.REST_DRILL;

import java.util.SortedSet;

public class LinkSupport {
	
	public static String makeDrillLink(String linkPrefix, String typeName, String id, String name, String linkPostfix) {
		if (linkPostfix == null) {
			linkPostfix = "";
		}
		return "<a href='" + linkPrefix + "/" + typeName + "/" + id + "/" + REST_DRILL + linkPostfix + "'>" + name + "</a>";
	}
	
	public static String buildParamSet(String paramName, SortedSet<String> paramSet) {
		
		if (paramSet == null || paramSet.isEmpty()) {
			return "";
		}
		
		StringBuffer buffer = null;
		for (String one : paramSet) {
			if (buffer == null) {
				buffer = new StringBuffer();
				buffer.append(paramName + "=" + one);
			} else {
				buffer.append("&" + paramName + "=" + one);
			}
		}
		return "?" + buffer.toString();
	}
	
	/*public static String buildParamSet(String paramName, List<String> paramList) {
		
		if (paramList == null || paramList.isEmpty()) {
			return "";
		}
		
		SortedSet<String> paramSet = new TreeSet<String>();
		paramSet.addAll(paramList);
		
		StringBuffer buffer = null;
		for (String one : paramSet) {
			if (buffer == null) {
				buffer = new StringBuffer();
				buffer.append(paramName + "=" + one);
			} else {
				buffer.append("&" + paramName + "=" + one);
			}
		}
		return buffer.toString();
	}*/
	
	/*public static String buildParamSet(Map<String, ?> paramMap) {
		
		if (paramMap == null || paramMap.isEmpty()) {
			return "";
		}
		
		SortedSet<String> paramSet = new TreeSet<String>();
		for (Entry<String, ?> entry : paramMap.entrySet()) {
			Object value = entry.getValue();
			if (value == null) {
				continue;
			}
			if (value instanceof String) {
				paramSet.add(entry.getKey() + "=" + value);
			} else if (value instanceof List<?>) {
				for (Object s : (List<?>) value) {
					if (!(s instanceof String)) {
						throw new RuntimeException();
					}
					paramSet.add(entry.getKey() + "=" + value);
				}
			} else {
				throw new RuntimeException();
			}
		}
		
		StringBuffer buffer = null;
		for (String one : paramSet) {
			if (buffer == null) {
				buffer = new StringBuffer();
				buffer.append(one);
			} else {
				buffer.append("&" + one);
			}
		}
		return buffer.toString();
	}*/
}
