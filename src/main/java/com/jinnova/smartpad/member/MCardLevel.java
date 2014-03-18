package com.jinnova.smartpad.member;

/**
 * bronze / gold / diamond
 *
 */
public class MCardLevel {

	private int level;

	private String name;
	
	public int getLevel() {
		return level;
	}
	
	public boolean qualify(MCard card) {
		return this.level <= card.getLevel().level;
	}
}
