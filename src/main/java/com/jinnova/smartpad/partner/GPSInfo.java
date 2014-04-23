package com.jinnova.smartpad.partner;

public class GPSInfo implements IGPSInfo {
	
	public static String INHERIT_PROVIDED = "provided";
	
	public static String INHERIT_BRANCH = "branch";
	
	public static String INHERIT_STORE = "store";

	private float longitude;
	
	private float latitude;
	
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
	public float getLongitude() {
		return longitude;
	}

	@Override
	public void setLongitude(float longitude) {
		this.longitude = longitude;
		inheritFrom = INHERIT_PROVIDED;
		//inheritFrom = null;
		modified = true;
	}

	@Override
	public float getLatitude() {
		return latitude;
	}

	@Override
	public void setLatitude(float latitude) {
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
