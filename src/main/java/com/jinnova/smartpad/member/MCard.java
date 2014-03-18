package com.jinnova.smartpad.member;

public class MCard {
	
	private String operationId;

	private int level;
	
	private int point;
	
	private int pointRotator;
	
	private String name;
	
	private String address;
	
	private String phone;
	
	private String email;
	
	public String getOperationId() {
		return operationId;
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
