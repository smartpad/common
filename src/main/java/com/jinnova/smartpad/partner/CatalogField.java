package com.jinnova.smartpad.partner;

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.JsonSupport;

public class CatalogField implements ICatalogField {

	private String id;
	
	private ICatalogFieldType fieldType;
	
	private boolean multivalue;
	
	private int groupingType = GROUPING_NONE;
	
	private int[] groupingRange;
	
	private int sectionNumber;

	private int groupNumber;
	
	private String name;
	
	private HashMap<String, Object> attributes = new HashMap<>();
	
	public static final String ATT_GROUPING = "gdata";
	public static final String ATT_GROUPING_VALUE = "v";
	public static final String ATT_GROUPING_VID = "vid";

	JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json.addProperty("ft", fieldType.name());
		json.addProperty("mv", multivalue);
		json.addProperty("gp", groupingType);
		json.add("gpr", JsonSupport.toJsonArray(groupingRange));
		json.addProperty("sn", sectionNumber);
		json.addProperty("gn", groupNumber);
		json.addProperty("n", name);
		json.add("at", JsonSupport.toJson(attributes));
		return json;
	}

	void populate(JsonElement jsonElement) {
		JsonObject json = jsonElement.getAsJsonObject();
		this.id = json.get("id").getAsString();
		this.fieldType = ICatalogFieldType.valueOf(json.get("ft").getAsString());
		this.multivalue = json.get("mv").getAsBoolean();
		this.groupingType = json.get("gp").getAsInt();
		this.groupingRange = JsonSupport.toArray(json, "gpr");
		this.sectionNumber = json.get("sn").getAsInt();
		this.groupNumber = json.get("gn").getAsInt();
		this.name = json.get("n").getAsString();
		this.attributes = JsonSupport.toHashmap(json.get("at").getAsJsonObject());
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isMultivalue() {
		return multivalue;
	}

	@Override
	public int getSectionNumber() {
		return sectionNumber;
	}
	
	@Override 
	public void setGroupingType(int groupingType) {
		this.groupingType = groupingType;
	}
	
	public int getGroupingType() {
		return this.groupingType;
	}

	@Override
	public int getGroupNumber() {
		return groupNumber;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setFieldType(ICatalogFieldType fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	@Override
	public void setSectionNumber(int sectionNumber) {
		this.sectionNumber = sectionNumber;
	}

	@Override
	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public ICatalogFieldType getFieldType() {
		return fieldType;
	}

	@Override
	public String getAttribute(String attributeId) {
		Object o = attributes.get(attributeId);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	@Override
	public void setAttribute(String attributeId, String value) {
		attributes.put(attributeId, value);
		
	}
	public Object getAttributeObject(String attributeId) {
		return attributes.get(attributeId);
	}
	
	public void setAttribute(String attributeId, JsonElement je) {
		attributes.put(attributeId, je);
		
	}
}
