package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.util.HashMap;
import java.util.StringTokenizer;

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
		json.addProperty(FIELD_SYSCATID, this.syscatId);
		json.addProperty(FIELD_NAME, this.getFieldValue(ICatalogField.F_NAME));
		json.addProperty(FIELD_DESC, this.getFieldValue(ICatalogField.F_DESC));
		
		if ((LAYOPT_WITHBRANCH & layoutOptions) == LAYOPT_WITHBRANCH) {
			json.addProperty(FIELD_BRANCHID, this.branchId);
			json.addProperty(FIELD_BRANCHNAME, this.branchName);
		}
		if ((LAYOPT_WITHCAT & layoutOptions) == LAYOPT_WITHCAT && !this.catalogId.equals(this.syscatId) &&
				
				//not showing catalog link if items is on branch/store's root catalog
				!this.catalogId.equals(this.branchId)) {
			
			json.addProperty(FIELD_CATID, this.catalogId);
			json.addProperty(FIELD_CATNAME, this.catName);
		}
		if ((LAYOPT_WITHSYSCAT & layoutOptions) == LAYOPT_WITHSYSCAT && (layoutSyscat == null || !this.syscatId.equals(layoutSyscat))) {
			json.addProperty(FIELD_SYSCATNAME, PartnerManager.instance.getSystemCatalog(syscatId).getName());
		}
		
		/*for (Entry<String, String> fieldValue : this.fieldValuesSingle.entrySet()) {
			String fn = fieldValue.getKey();
			if (fn.equals(ICatalogField.F_NAME) || fn.equals(ICatalogField.F_DESC)) {
				continue;
			}
			json.addProperty(fn, fieldValue.getValue());
		}
		
		for (Entry<String, String[]> fieldValues : this.fieldValuesMulti.entrySet()) {
			String fn = fieldValues.getKey();
			if (fn.equals(ICatalogField.F_NAME) || fn.equals(ICatalogField.F_DESC)) {
				continue;
			}
			json.add(fn, JsonSupport.toJsonArray(fieldValues.getValue()));
		}*/
		
		if ((LAYOPT_WITHDETAILS & layoutOptions) == LAYOPT_WITHDETAILS) {
			final ICatalogSpec spec = PartnerManager.instance.getCatalogSpec(syscatId);
			String displayPattern = spec.getAttribute(ICatalogSpec.ATT_DISP_DETAIL);
			if (displayPattern != null) {
				displayPattern = SmartpadCommon.replace(displayPattern, new ReplaceSupport() {
					
					@Override
					public String getTerm(String fieldId) {
	
						String fieldValue;
						if (fieldId.startsWith("-")) {
							fieldId = fieldId.substring(1);
							fieldValue = getFieldValue(fieldId);
						} else if (fieldId.startsWith("segmentLink:")) {
							fieldId = fieldId.substring("segmentLink:".length());
							StringTokenizer tokens = new StringTokenizer(fieldId, ",");
							String segmentParam = null;
							while (tokens.hasMoreTokens()) {
								String oneSegmentField = tokens.nextToken();
								String oneParam = "segments=" + oneSegmentField + 
										ICatalogField.SEGMENT_PARAM_SEP + SmartpadCommon.md5(getFieldValue(oneSegmentField));
								if (segmentParam == null) {
									segmentParam = oneParam;
								} else {
									segmentParam = segmentParam + "&" + oneParam;
								}
							}
							fieldValue = "/w/syscat/" + syscatId + "/drill?" + segmentParam;
						} else {
							fieldValue = getFieldValue(fieldId);
							CatalogField field = (CatalogField) spec.getField(fieldId);
							if (field.getGroupingType() != ICatalogField.SEGMENT_NONE) {
								//String segmentId = getFieldValue(fieldId + ICatalogField.GROUPING_POSTFIX);
								String segmentId = SmartpadCommon.md5(fieldValue);
								fieldValue = "<a href='/w/syscat/" + syscatId + "/drill?segments=" + 
										fieldId + ICatalogField.SEGMENT_PARAM_SEP + segmentId + "'>" +
										fieldValue + "</a>";
							}
						}
						return fieldValue;
					}
				});
				json.addProperty(FIELD_DISP, displayPattern);
			}
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
