package com.jinnova.smartpad.partner;

import com.jinnova.smartpad.member.MCardLevel;

public class Promotion {

	private Schedule schedule;
	
	private MCardLevel requiredLevel;
	
	public boolean qualify(Consumer consumer) {
		if (requiredLevel != null) {
			if (!consumer.qualify(requiredLevel)) {
				return false;
			}
		}
		
		return true;
	}
}
