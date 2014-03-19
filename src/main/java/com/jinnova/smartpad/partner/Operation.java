package com.jinnova.smartpad.partner;

import java.util.HashMap;
import java.util.LinkedList;

public class Operation implements IOperation {
	
	//private final String operationId;

	private Schedule openingHours;
	
	private Address address;
	
	private String phone;
	
	private String email;

	private Catalog rootCatalog;
	
	/**
	 * member cards
	 */
	private LinkedList<Integer> memberLevels;
	
	private HashMap<Integer, String> memberLevelNames;
	
	private boolean memberNameRequired = false;
	
	private boolean memberAddressRequired = false;
	
	private boolean memberPhoneRequired = false;
	
	private boolean memberEmailRequired = false;
	
	private boolean memberOfferedFree = false;
	
	private int memberOfferedFreeLevel;
	
	private String memberOfferedSurvey;
	
	private int memberOfferedSurveyLevel;
	
	/*public Operation(String operationId) {
		this.operationId = operationId;
	}*/

}
