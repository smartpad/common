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
	private LinkedList<Integer> mcardLevels;
	
	private HashMap<Integer, String> mcardLevelNames;
	
	private boolean mcardNameRequired = false;
	
	private boolean mcardAddressRequired = false;
	
	private boolean mcardPhoneRequired = false;
	
	private boolean mcardEmailRequired = false;
	
	private boolean mcardOfferedFree = false;
	
	private int mcardOfferedFreeLevel;
	
	private String mcardOfferedSurvey;
	
	private int mcardOfferedSurveyLevel;

}
