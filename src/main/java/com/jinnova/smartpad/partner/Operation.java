package com.jinnova.smartpad.partner;

import java.util.LinkedList;

public class Operation implements IOperation {
	
	public static final String STORE_MAIN_ID = "BRANCH";
	
	private final String branchId;
	
	private String storeId;
	
	private boolean persisted;
	
	private String name;

	private final Schedule openHours = new Schedule();
	
	private String phone;
	
	private String email;

	//private Catalog rootCatalog;

	private long gpsLon;

	private long gpsLat;
	
	/*private String numberStreet;
	
	private String ward;
	
	private String district;
	
	private String city;*/
	
	private String addressLines;
	
	/**
	 * member cards
	 */
	private final LinkedList<String> memberLevels = new LinkedList<String>();
	
	private Boolean memberNameRequired = false;
	
	private Boolean memberAddressRequired = false;
	
	private Boolean memberPhoneRequired = false;
	
	private Boolean memberEmailRequired = false;
	
	private Boolean memberOfferedFree = false;
	
	private Integer memberOfferedFreeLevel;
	
	private String memberOfferedSurvey;
	
	private Integer memberOfferedSurveyLevel;
	
	public Operation(String branchId, /*String storeId,*/ boolean persisted) {
		this.branchId = branchId;
		//this.storeId = storeId;
		this.persisted = persisted;
	}
	
	public String getBranchId() {
		return this.branchId;
	}
	
	public String getStoreId() {
		return this.storeId;
	}
	
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	
	boolean isPersisted() {
		return this.persisted;
	}
	
	void setPersisted(boolean b) {
		this.persisted = b;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Schedule getOpenHours() {
		return openHours;
	}
	
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String[] getMemberLevels() {
		return memberLevels.toArray(new String[memberLevels.size()]);
	}

	public void setMemberLevels(String[] levels) {
		this.memberLevels.clear();
		for (String s : levels) {
			this.memberLevels.add(s);
		}
	}

	public Boolean isMemberNameRequired() {
		return memberNameRequired;
	}

	public void setMemberNameRequired(boolean memberNameRequired) {
		this.memberNameRequired = memberNameRequired;
	}

	public Boolean isMemberAddressRequired() {
		return memberAddressRequired;
	}

	public void setMemberAddressRequired(boolean memberAddressRequired) {
		this.memberAddressRequired = memberAddressRequired;
	}

	public Boolean isMemberPhoneRequired() {
		return memberPhoneRequired;
	}

	public void setMemberPhoneRequired(boolean memberPhoneRequired) {
		this.memberPhoneRequired = memberPhoneRequired;
	}

	public Boolean isMemberEmailRequired() {
		return memberEmailRequired;
	}

	public void setMemberEmailRequired(boolean memberEmailRequired) {
		this.memberEmailRequired = memberEmailRequired;
	}

	public Boolean isMemberOfferedFree() {
		return memberOfferedFree;
	}

	public void setMemberOfferedFree(boolean memberOfferedFree) {
		this.memberOfferedFree = memberOfferedFree;
	}

	public Integer getMemberOfferedFreeLevel() {
		return memberOfferedFreeLevel;
	}

	public void setMemberOfferedFreeLevel(int memberOfferedFreeLevel) {
		this.memberOfferedFreeLevel = memberOfferedFreeLevel;
	}

	public String getMemberOfferedSurvey() {
		return memberOfferedSurvey;
	}

	public void setMemberOfferedSurvey(String memberOfferedSurvey) {
		this.memberOfferedSurvey = memberOfferedSurvey;
	}

	public Integer getMemberOfferedSurveyLevel() {
		return memberOfferedSurveyLevel;
	}

	public void setMemberOfferedSurveyLevel(int memberOfferedSurveyLevel) {
		this.memberOfferedSurveyLevel = memberOfferedSurveyLevel;
	}

}
