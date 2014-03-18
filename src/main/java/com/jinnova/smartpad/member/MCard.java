package com.jinnova.smartpad.member;

public class MCard {

	private MCardLevel level;
	
	private int point;
	
	private int pointRotator;

	public MCardLevel getLevel() {
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
