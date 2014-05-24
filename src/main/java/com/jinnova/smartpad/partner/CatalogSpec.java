package com.jinnova.smartpad.partner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jinnova.smartpad.JsonSupport;

public class CatalogSpec implements ICatalogSpec {
	
	private String referTo;
	
	private String specId;
	
	private final TreeMap<String, ICatalogField> allFields = new TreeMap<>();
	
	private final LinkedList<String> sectionNames = new LinkedList<>();
	
	private final LinkedList<String> groupNames = new LinkedList<>();
	
	private HashMap<String, String> attributes = new HashMap<>();
	
	private HashSet<String> attributeHiddenSegments;
	
	private boolean managed;
	
	public CatalogSpec() {
		
	}

	@Override
	public String getSpecId() {
		return specId;
	}

	@Override
	public void setSpecId(String specId) {
		this.specId = specId;
	}
	
	@Override
	public boolean isManaged() {
		return this.managed;
	}
	
	public void setManaged(boolean b) {
		this.managed = b;
	}
	
	@Override
	public void setReferTo(String referTo) {
		this.referTo = referTo;
	}
	
	public String getReferTo() {
		return this.referTo;
	}

	@Override
	public String[] getSectionNames() {
		return sectionNames.toArray(new String[sectionNames.size()]);
	}

	@Override
	public String[] getGroupNames() {
		return groupNames.toArray(new String[groupNames.size()]);
	}

	@Override
	public ICatalogField[] getAllFields() {
		return allFields.values().toArray(new ICatalogField[allFields.size()]);
	}

	@Override
	public ICatalogField getField(String fieldId) {
		return allFields.get(fieldId);
	}
	
	public LinkedList<CatalogField> getGroupingFields() {
		LinkedList<CatalogField> fields = new LinkedList<>();
		for (ICatalogField f : allFields.values()) {
			CatalogField cf = (CatalogField) f;
			if (cf.getGroupingType() != ICatalogField.SEGMENT_NONE) {
				fields.add(cf);
			}
		}
		return fields;
	}

	@Override
	public ICatalogField createField(String fieldId) {
		CatalogField field = new CatalogField();
		field.setId(fieldId);
		allFields.put(fieldId, field);
		return field;
	}
	
	public JsonObject toJson() {
		
		if (referTo != null) {
			JsonObject json = new JsonObject();
			json.addProperty("referTo", referTo);
			return json;
		}
		
		if (allFields.isEmpty()) {
			return null;
		}
		
		JsonObject json = new JsonObject();
		json.add("sid", new JsonPrimitive(specId));
		json.addProperty("managed", managed);
		
		JsonArray ja = new JsonArray();
		for (String s : sectionNames) {
			ja.add(new JsonPrimitive(s));
		}
		json.add("sNames", ja);
		
		ja = new JsonArray();
		for (String s : groupNames) {
			ja.add(new JsonPrimitive(s));
		}
		json.add("gNames", ja);
		
		ja = new JsonArray();
		for (ICatalogField f : allFields.values()) {
			ja.add(((CatalogField) f).toJson());
		}
		json.add("fields", ja);
		
		json.add("atts", JsonSupport.toJson(attributes));
		return json;
	}
	
	public void populate(JsonObject json) {
		
		referTo = JsonSupport.getAsString(json, "referTo");
		if (referTo != null) {
			return;
		}
		
		specId = json.get("sid").getAsString();
		managed = json.get("managed").getAsBoolean();
		JsonArray ja = json.get("sNames").getAsJsonArray();
		for (int i = 0; i < ja.size(); i++) {
			sectionNames.add(ja.get(i).getAsString());
		}
		ja = json.get("gNames").getAsJsonArray();
		for (int i = 0; i < ja.size(); i++) {
			groupNames.add(ja.get(i).getAsString());
		}
		ja = json.get("fields").getAsJsonArray();
		for (int i = 0; i < ja.size(); i++) {
			CatalogField field = new CatalogField();
			field.populate(ja.get(i));
			allFields.put(field.getId(), field);
		}
		this.attributes = JsonSupport.toHashmap(json.get("atts").getAsJsonObject());
	}

	public boolean isSegmentHidden(String segmentField) {
		if (attributeHiddenSegments == null) {
			attributeHiddenSegments = new HashSet<>();
			String hiddenSegmentFields = attributes.get(ICatalogSpec.ATT_DISP_SEGMENTS_HIDDEN);
			if (hiddenSegmentFields != null) {
				StringTokenizer tokens = new StringTokenizer(hiddenSegmentFields, ", ");
				while (tokens.hasMoreTokens()) {
					String one = tokens.nextToken();
					attributeHiddenSegments.add(one);
				}
			}
		}
		return attributeHiddenSegments.contains(segmentField);
	}

	@Override
	public String getAttribute(String attributeId) {
		return attributes.get(attributeId);
	}

	@Override
	public void setAttribute(String attributeId, String value) {
		attributes.put(attributeId, value);
	}
}
