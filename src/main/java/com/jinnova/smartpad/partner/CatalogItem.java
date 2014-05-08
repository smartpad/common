package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.RecordInfo;

public class CatalogItem implements ICatalogItem, Feed {
	
	private String branchId;
	
	public final String storeId;
	
	//public final Catalog catalog;
	private String catalogId;
	
	private String syscatId;
	
	private String itemId;
	
	//private final Name name = new Name();
	
	//private String unit;
	
	//private BigDecimal unitPrice;
	
	private String branchName;
	
	private String catName;

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
	
	public void setCatalogId(String catId) {
		this.catalogId = catId;
	}
	
	public String getSyscatId() {
		return this.syscatId;
	}
	
	public String getBranchId() {
		return this.branchId;
	}
	
	public void setBranchId(String id) {
		this.branchId = id;
	}

	@Override
	public void setBranchName(String bn) {
		this.branchName = bn;
	}
	
	@Override
	public String getBranchName() {
		return this.branchName;
	}
	
	public void setCatalogName(String catName) {
		this.catName = catName;
	}
	
	public String getCatalogName() {
		return this.catName;
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

	@Override
	public JsonObject generateFeedJson(int layoutOptions, String layoutSyscat) {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_ID, this.itemId);
		json.addProperty(FIELD_TYPE, IDetailManager.TYPENAME_CATITEM);
		json.addProperty(FIELD_NAME, this.getFieldValue(ICatalogField.F_NAME));
		json.addProperty(FIELD_SYSCATID, this.syscatId);
		if ((LAYOPT_WITHBRANCH & layoutOptions) == LAYOPT_WITHBRANCH) {
			json.addProperty(FIELD_BRANCHID, this.branchId);
			json.addProperty(FIELD_BRANCHNAME, this.branchName);
		}
		if ((LAYOPT_WITHCAT & layoutOptions) == LAYOPT_WITHCAT && !this.catalogId.equals(this.syscatId)) {
			json.addProperty(FIELD_CATID, this.catalogId);
			json.addProperty(FIELD_CATNAME, this.catName);
		}
		if ((LAYOPT_WITHSYSCAT & layoutOptions) == LAYOPT_WITHSYSCAT && (layoutSyscat == null || !this.syscatId.equals(layoutSyscat))) {
			json.addProperty(FIELD_SYSCATNAME, PartnerManager.instance.getSystemCatalog(syscatId).getName());
		}
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
