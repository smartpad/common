package com.jinnova.smartpad.partner;

import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CatalogSpec implements ICatalogSpec {
	
	private String specId;
	
	private final LinkedList<ICatalogField> allFields = new LinkedList<>();
	
	private final LinkedList<String> sectionNames = new LinkedList<>();
	
	private final LinkedList<String> groupNames = new LinkedList<>();
	
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
	public String[] getSectionNames() {
		return sectionNames.toArray(new String[sectionNames.size()]);
	}

	@Override
	public String[] getGroupNames() {
		return groupNames.toArray(new String[groupNames.size()]);
	}

	@Override
	public ICatalogField[] getAllFields() {
		return allFields.toArray(new ICatalogField[allFields.size()]);
	}

	@Override
	public ICatalogField createField() {
		CatalogField field = new CatalogField();
		allFields.add(field);
		return field;
	}
	
	public JsonObject toJson() {
		
		if (allFields.isEmpty()) {
			return null;
		}
		
		JsonObject json = new JsonObject();
		json.add("sid", new JsonPrimitive(specId));
		
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
		for (ICatalogField f : allFields) {
			ja.add(((CatalogField) f).toJson());
		}
		json.add("fields", ja);
		return json;
	}
	
	public void populate(JsonObject json) {
		
		specId = json.get("sid").getAsString();
		
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
			allFields.add(field);
		}
	}
}
