package com.jinnova.smartpad.member;

import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.partner.IRecordInfo;
import com.jinnova.smartpad.partner.SmartpadCommon;

public class Member implements IMember {
	
	private String id;
	
	private String name;
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	public String generateId() {
		return SmartpadCommon.md5(name + System.currentTimeMillis());
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public Member(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}

}

