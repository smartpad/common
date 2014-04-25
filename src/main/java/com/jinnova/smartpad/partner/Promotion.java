package com.jinnova.smartpad.partner;

import java.util.Date;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.member.CCardRequirement;
import com.jinnova.smartpad.member.Consumer;

public class Promotion implements IPromotion, Feed {
	
	private String promotionId;
	
	public final String branchId;
	
	public final String storeId;
	
	public final String syscatId;
	
	private final Name name = new Name();
	
	public final GPSInfo gps = new GPSInfo();
	
	private final RecordInfo recordInfo = new RecordInfo();

	private Schedule schedule;
	
	private int requiredMemberLevel;
	
	private int requiredMemberPoint;
	
	private CCardRequirement[] requiredCCardOptions;
	
	private final MCardOffer mcardOffer = new MCardOffer();

	public Promotion(String promotionId, String branchId, String storeId, String syscatId) {
		this.promotionId = promotionId;
		this.branchId = branchId;
		this.storeId = storeId;
		this.syscatId = syscatId;
	}

	@Override
	public String getId() {
		return this.promotionId;
	}

	boolean isPersisted() {
		return promotionId != null;
	}
	
	public boolean qualify(Consumer consumer) {
		if (!consumer.qualify(branchId, storeId, requiredMemberLevel, requiredMemberPoint)) {
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
				if (!consumer.qualify(opt.requiredCCardType, opt.requiredCCardBranch, opt.requiredCCardIssuer)) {
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
	public IGPSInfo getGps() {
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
