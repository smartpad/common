package com.jinnova.smartpad.partner;

import java.math.BigDecimal;

public class CatalogItem implements ICatalogItem {
	
	private String itemId;
	
	private final Name name = new Name();
	
	private String unit;
	
	private BigDecimal unitPrice;
	
	public CatalogItem(String itemId) {
		this.itemId = itemId;
	}
	
	String getItemId() {
		return this.itemId;
	}
	
	void setItemId(String s) {
		this.itemId = s;
	}
	
	@Override
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
	}
	
}
