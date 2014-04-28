package com.jinnova.smartpad.member;

import com.google.gson.JsonObject;

public class CCardRequirement {
	
	public CCardType requiredCCardType;
	
	public CCardBranch requiredCCardBranch;
	
	public String requiredCCardIssuer;
	
	public JsonObject getJson() {
		JsonObject json = new JsonObject();
		json.addProperty("t", requiredCCardType.name());
		json.addProperty("b", requiredCCardBranch.name());
		json.addProperty("i", requiredCCardIssuer);
		return json;
	}
	
	public void readJson(JsonObject json) {
		this.requiredCCardType = CCardType.valueOf(CCardType.class, json.get("t").getAsString());
		this.requiredCCardBranch = CCardType.valueOf(CCardBranch.class, json.get("b").getAsString());
		this.requiredCCardIssuer = json.get("i").getAsString();
	}

}
