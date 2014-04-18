package com.jinnova.smartpad.drilling;

class ActionLoad {
	
	static final String REL_SIMILAR = "sim";
	
	static final String REL_BELONG = "bel";
	
	static final String REL_SIBLING = "sib";
	
	String anchorType;
	
	String anchorId;

	String targetType;
	
	String relation;
	
	int offset;
	
	int size;
	
	String excludeId;

	ActionLoad(String anchorType, String anchorId, String targetType, String rel, int size) {
		super();
		this.anchorType = anchorType;
		this.anchorId = anchorId;
		this.targetType = targetType;
		this.relation = rel;
		this.size = size;
	}
	
	ActionLoad exclude(String excludeId) {
		this.excludeId = excludeId;
		return this;
	}

	String generateNextLoadUrl() {
		return targetType + "/" + relation + "?anchorType=" + anchorType + "&anchorId=" + anchorId + "&offset=" + offset + "&size="  + size;
	}
}
