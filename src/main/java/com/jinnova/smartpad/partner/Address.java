package com.jinnova.smartpad.partner;

public class Address implements IAddress {

	private long gpsLon;

	private long gpsLat;
	
	/*private String numberStreet;
	
	private String ward;
	
	private String district;
	
	private String city;*/
	
	private String addressLines;
	
	public long getGpsLon() {
		return gpsLon;
	}

	public void setGpsLon(long gpsLon) {
		this.gpsLon = gpsLon;
	}

	public long getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(long gpsLat) {
		this.gpsLat = gpsLat;
	}

	public String getAddressLines() {
		return addressLines;
	}

	public void setAddressLines(String addressLines) {
		this.addressLines = addressLines;
	}
}
