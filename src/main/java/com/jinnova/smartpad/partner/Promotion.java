package com.jinnova.smartpad.partner;

import java.util.Date;

import com.jinnova.smartpad.member.CCardBranch;
import com.jinnova.smartpad.member.CCardIssuer;
import com.jinnova.smartpad.member.CCardType;
import com.jinnova.smartpad.member.Consumer;

public class Promotion {
	
	private String operationId;

	private Schedule schedule;
	
	private int requiredMemberLevel;
	
	private int requiredMemberPoint;
	
	private CCardType requiredCreditType;
	
	private CCardBranch requiredCreditBranch;
	
	private CCardIssuer requiredCreditIssuer;
	
	public boolean qualify(Consumer consumer) {
		if (!consumer.qualify(operationId, requiredMemberLevel, requiredMemberPoint)) {
			return false;
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
