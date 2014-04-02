package com.jinnova.smartpad.partner;

import java.util.HashMap;

import com.jinnova.smartpad.RecordInfo;

public class CatalogItem implements ICatalogItem {
	
	public final Catalog catalog;
	
	private String itemId;
	
	//private final Name name = new Name();
	
	//private String unit;
	
	//private BigDecimal unitPrice;

	private final RecordInfo recordInfo = new RecordInfo();
	
	private final HashMap<String, String> fieldValuesSingle = new HashMap<>();
	
	private final HashMap<String, String[]> fieldValuesMulti = new HashMap<>(); 
	
	public CatalogItem(Catalog catalog, String itemId) {
		this.catalog = catalog;
		this.itemId = itemId;
	}
	
	String getItemId() {
		return this.itemId;
	}
	
	void setItemId(String s) {
		this.itemId = s;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}

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
