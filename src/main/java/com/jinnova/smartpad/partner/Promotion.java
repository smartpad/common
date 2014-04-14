package com.jinnova.smartpad.partner;

import java.util.Date;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.member.Consumer;

public class Promotion implements IPromotion {
	
	private String promotionId;
	
	private final String operationId;
	
	private final Name name = new Name();
	
	public final GPSInfo gps = new GPSInfo();
	
	private final RecordInfo recordInfo = new RecordInfo();

	private Schedule schedule;
	
	private int requiredMemberLevel;
	
	private int requiredMemberPoint;
	
	private CCardRequirement[] requiredCCardOptions;
	
	private final MCardOffer mcardOffer = new MCardOffer();

	public Promotion(String promotionId, String operationId) {
		this.promotionId = promotionId;
		this.operationId = operationId;
	}

	@Override
	public String getId() {
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
		
		if (requiredCCardOptions != null) {
			boolean ok = false;
			for (CCardRequirement opt : requiredCCardOptions) {
				if (!consumer.qualify(opt.requiredCreditType, opt.requiredCreditBranch, opt.requiredCreditIssuer)) {
					ok = true;
				}
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IName getName() {
		return name;
	}

	@Override
	public GPSInfo getGps() {
		return gps;
	}
	
	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}
	
	public boolean isMemberCardOffered() {
		return mcardOffer.memberOfferedLevel != null;
	}
	
	public MCardOffer getMemberCardOffer() {
		return mcardOffer;
	}

	public JsonObject generateFeedJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", this.promotionId);
		json.addProperty("type", IDetailManager.TYPENAME_PROMO);
		json.addProperty("name", name.getName());
		return json;
	}
}
