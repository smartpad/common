package com.jinnova.smartpad.partner;

import java.util.LinkedList;

public class Branch {
	
	private String id;
	
	private Operation operation;
	
	private LinkedList<Store> allStores;
	
	private User primaryUser;
	
	public String getId() {
		return this.id;
	}
}
