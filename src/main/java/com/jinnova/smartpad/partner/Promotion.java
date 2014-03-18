package com.jinnova.smartpad.partner;

import java.util.Date;

import com.jinnova.smartpad.creditcard.CCardBranch;
import com.jinnova.smartpad.creditcard.CCardIssuer;
import com.jinnova.smartpad.creditcard.CCardType;
import com.jinnova.smartpad.member.MCardLevel;

public class Promotion {

	private Schedule schedule;
	
	private MCardLevel requiredLevel;
	
	private CCardType requiredCreditType;
	
	private CCardBranch requiredCreditBranch;
	
	private CCardIssuer requiredCreditIssuer;
	
	public boolean qualify(Consumer consumer) {
		if (requiredLevel != null) {
			if (!consumer.qualify(requiredLevel)) {
				return false;
			}
		}
		
		if (schedule != null) {
			if (!schedule.isInAffect(new Date())) {
				return false;
			}
		}
		if (!consumer.qualify(requiredCreditType, requiredCreditBranch, requiredCreditIssuer)) {
			return false;
		}
		return true;
	}
}
