package com.jinnova.smartpad.partner;

import java.util.LinkedList;

public class Operation implements IOperation {
	
	static final String STORE_MAIN_ID = "BRANCH";
	
	public final String branchId;
	
	public final String storeId;
	
	private boolean persisted;
	
	private String name;

	private final Schedule openingHours = new Schedule();
	
	private final Address address = new Address();
	
	private String phone;
	
	private String email;

	//private Catalog rootCatalog;
	
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
	
	public Operation(String branchId, String storeId, boolean persisted) {
		this.branchId = branchId;
		this.storeId = storeId;
		this.persisted = persisted;
	}
	
	boolean isPersisted() {
		return this.persisted;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Schedule getOpeningHours() {
		return openingHours;
	}

	public IAddress getAddress() {
		return address;
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

	public LinkedList<String> getMemberLevels() {
		return memberLevels;
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
