package com.jinnova.smartpad.member;

/**
 * bronze / gold / diamond
 *
 */
public class MCardLevel {
	
	private String operationId;

	private int level;

	private String name;
	
	public int getLevel() {
		return level;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOperationId() {
		return this.operationId;
	}
	
	public boolean qualify(MCard card) {
		return this.level <= card.getLevel().level;
	}
}
