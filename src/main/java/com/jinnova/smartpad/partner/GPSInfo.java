package com.jinnova.smartpad.partner;

import java.math.BigDecimal;

public class GPSInfo implements IGPSInfo {
	
	public static String INHERIT_PROVIDED = "provided";
	
	public static String INHERIT_BRANCH = "branch";
	
	public static String INHERIT_STORE = "store";

	private BigDecimal longitude;
	
	private BigDecimal latitude;
	
	private String inheritFrom;
	
	private boolean modified = false;
	
	void inherit(GPSInfo other, String inheritFrom) {
		this.longitude = other.longitude;
		this.latitude = other.latitude;
		if (/*other.inheritFrom == null*/INHERIT_PROVIDED.equals(other.inheritFrom)) {
			this.inheritFrom = inheritFrom;
		} else {
			this.inheritFrom = other.inheritFrom;
		}
	}

	@Override
	public BigDecimal getLongitude() {
		return longitude;
	}

	@Override
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
		inheritFrom = INHERIT_PROVIDED;
		//inheritFrom = null;
		modified = true;
	}

	@Override
	public BigDecimal getLatitude() {
		return latitude;
	}

	@Override
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
		inheritFrom = INHERIT_PROVIDED;
		//inheritFrom = null;
		modified = true;
	}

	public String getInheritFrom() {
		return inheritFrom;
	}

	public void setInheritFrom(String inheritFrom) {
		this.inheritFrom = inheritFrom;
	}
	
	public void clearModifiedFlag() {
		this.modified = false;
	}
	
	boolean isModified() {
		return this.modified;
	}

}
