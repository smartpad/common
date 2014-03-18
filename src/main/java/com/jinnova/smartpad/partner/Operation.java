package com.jinnova.smartpad.partner;

import java.util.HashMap;
import java.util.LinkedList;

public class Operation {

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

}
