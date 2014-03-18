package com.jinnova.smartpad.partner;

import java.util.LinkedList;

import com.jinnova.smartpad.partner.IUser;

public class Branch implements IBranch {
	
	private String id;
	
	private String name;
	
	private Operation operation;
	
	private LinkedList<Store> allStores;
	
	private IUser primaryUser;
	
	public Branch(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}
}
