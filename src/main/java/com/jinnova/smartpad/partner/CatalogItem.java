package com.jinnova.smartpad.partner;

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.RecordInfo;

public class CatalogItem implements ICatalogItem {
	
	public final String branchId;
	
	public final String storeId;
	
	//public final Catalog catalog;
	private String catalogId;
	
	private String syscatId;
	
	private String itemId;
	
	//private final Name name = new Name();
	
	//private String unit;
	
	//private BigDecimal unitPrice;

	private final RecordInfo recordInfo = new RecordInfo();
	
	public final GPSInfo gps = new GPSInfo();
	
	private final HashMap<String, String> fieldValuesSingle = new HashMap<>();
	
	private final HashMap<String, String[]> fieldValuesMulti = new HashMap<>(); 
	
	public CatalogItem(String branchId, String storeId, String catalogId, String syscatId, String itemId) {
		this.branchId = branchId;
		this.storeId = storeId;
		this.catalogId = catalogId;
		this.syscatId = syscatId;
		this.itemId = itemId;
	}
	
	@Override
	public String getId() {
		return this.itemId;
	}
	
	void setId(String s) {
		this.itemId = s;
	}
	
	public String getCatalogId() {
		return this.catalogId;
	}
	
	public String getSyscatId() {
		return this.syscatId;
	}
	
	@Override
	public IGPSInfo getGps() {
		return this.gps;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}

	/* fieldId from ICatalogField
	 */
	@Override
	public String getFieldValue(String fieldId) {
		return fieldValuesSingle.get(fieldId);
	}

	@Override
	public String[] getFieldValues(String fieldId) {
		return fieldValuesMulti.get(fieldId);
	}

	@Override
	public void setField(String fieldId, String value) {
		fieldValuesSingle.put(fieldId, value);
	}

	@Override
	public void setField(String fieldId, String[] values) {
		fieldValuesMulti.put(fieldId, values);
	}

	public JsonElement generateFeedJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", this.itemId);
		json.addProperty("type", IDetailManager.TYPENAME_CATITEM);
		json.addProperty("name", this.getFieldValue(ICatalogField.ID_NAME));
		return json;
	}
	
	/*@Override
	public IName getName() {
		return this.name;
	}

	@Override
	public String getUnit() {
		return unit;
	}

	@Override
	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	@Override
	public void setUnitPrice(BigDecimal price) {
		this.unitPrice = price;
	}*/
	
}
