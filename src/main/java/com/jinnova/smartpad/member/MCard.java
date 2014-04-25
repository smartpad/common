package com.jinnova.smartpad.member;

@SuppressWarnings("unused")
public class MCard {
	
	private String branchId;
	
	private String storeId;

	private int level;
	
	private int point;
	
	private int pointRotator;
	
	private String name;
	
	private String address;
	
	private String phone;
	
	private String email;
	
	public String getBranchId() {
		return branchId;
	}
	
	public String getStoreId() {
		return storeId;
	}

	public int getLevel() {
		return level;
	}
	
	public int getPoint() {
		return point;
	}
	
	public void addPoint(int p) {
		point += p;
		point = point % pointRotator;
	}
}
