package com.jinnova.smartpad.partner;

import java.util.Date;

import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.member.CCardBranch;
import com.jinnova.smartpad.member.CCardIssuer;
import com.jinnova.smartpad.member.CCardType;
import com.jinnova.smartpad.member.Consumer;

public class Promotion implements IPromotion {
	
	private String promotionId;
	
	private final String operationId;
	
	private final Name name = new Name();
	
	private final RecordInfo recordInfo = new RecordInfo();

	private Schedule schedule;
	
	private int requiredMemberLevel;
	
	private int requiredMemberPoint;
	
	private CCardType requiredCreditType;
	
	private CCardBranch requiredCreditBranch;
	
	private CCardIssuer requiredCreditIssuer;
	
	public Promotion(String promotionId, String operationId) {
		this.promotionId = promotionId;
		this.operationId = operationId;
	}

	String getPromotionId() {
		return this.promotionId;
	}

	boolean isPersisted() {
		return promotionId != null;
	}
	
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

	@Override
	public IName getName() {
		return name;
	}
	
	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}
}
