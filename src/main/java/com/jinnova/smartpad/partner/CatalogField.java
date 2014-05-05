package com.jinnova.smartpad.partner;

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.JsonSupport;

public class CatalogField implements ICatalogField {

	private String id;
	
	private ICatalogFieldType fieldType;
	
	private boolean multivalue;
	
	private int sectionNumber;

	private int groupNumber;
	
	private String name;
	
	private HashMap<String, String> attributes = new HashMap<>();

	JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json.addProperty("mv", multivalue);
		json.addProperty("ft", fieldType.name());
		json.addProperty("sn", sectionNumber);
		json.addProperty("gn", groupNumber);
		json.add("at", JsonSupport.toJson(attributes));
		return json;
	}

	void populate(JsonElement jsonElement) {
		JsonObject json = jsonElement.getAsJsonObject();
		this.id = json.get("id").getAsString();
		this.multivalue = json.get("mv").getAsBoolean();
		this.fieldType = ICatalogFieldType.valueOf(json.get("ft").getAsString());
		this.sectionNumber = json.get("sn").getAsInt();
		this.groupNumber = json.get("gn").getAsInt();
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
		return attributes.get(attributeId);
	}

	@Override
	public void setAttribute(String attributeId, String value) {
		attributes.put(attributeId, value);
		
	}
}
