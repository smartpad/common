package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.util.Date;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.member.CCardBranch;
import com.jinnova.smartpad.member.CCardRequirement;
import com.jinnova.smartpad.member.CCardType;
import com.jinnova.smartpad.member.Consumer;

public class Promotion implements IPromotion, Feed {
	
	private String promotionId;
	
	public final String branchId;
	
	public final String storeId;
	
	public final String syscatId;
	
	private final Name name = new Name();
	
	public final GPSInfo gps = new GPSInfo();
	
	private final RecordInfo recordInfo = new RecordInfo();

	private final Schedule schedule = new Schedule();
	
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
	
	public String getCCardOptionsJson() {
		if (this.requiredCCardOptions == null) {
			return null;
		}
		JsonArray ja = new JsonArray();
		for (CCardRequirement opt : requiredCCardOptions) {
			ja.add(opt.getJson());
		}
		return ja.toString();
	}
	
	public void readCCardOptions(JsonArray ja) {
		if (ja == null) {
			return;
		}
		
		this.requiredCCardOptions = new CCardRequirement[ja.size()];
		for (int i = 0; i < ja.size(); i++) {
			requiredCCardOptions[i] = new CCardRequirement();
			requiredCCardOptions[i].readJson(ja.get(i).getAsJsonObject());
		}
	}
	
	public CCardRequirement getCCardOpt(CCardBranch cb, CCardType ct) {
		if (this.requiredCCardOptions == null) {
			return null;
		}
		
		for (CCardRequirement opt : requiredCCardOptions) {
			if (opt.requiredCCardBranch == cb && opt.requiredCCardType == ct) {
				return opt;
			}
		}
		return null;
	}
	
	@Override
	public Schedule getSchedule() {
		return this.schedule;
	}
	
	public int getRequiredMemberLevel() {
		return this.requiredMemberLevel;
	}
	
	public void setRequiredMemberLevel(int level) {
		this.requiredMemberLevel = level;
	}
	
	public int getRequiredMemberPoint() {
		return this.requiredMemberPoint;
	}
	
	public void setRequiredMemberPoint(int point) {
		this.requiredMemberPoint = point;
	}
	
	public boolean qualify(Consumer consumer) {
		if (!consumer.qualify(branchId, storeId, requiredMemberLevel, requiredMemberPoint)) {
			return false;
		}
		
		//if (schedule != null) {
			if (!schedule.isInAffect(new Date())) {
				return false;
			}
		//}
		
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
	public IName getDesc() {
		return name;
	}
	
	@Override
	public String getName() {
		return this.name.getName();
	}
	
	@Override
	public void setName(String s) {
		this.name.setName(s);
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

	public JsonObject generateFeedJson(int layoutOptions, HashMap<String, Object> layoutParams) {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_ID, this.promotionId);
		json.addProperty(FIELD_TYPE, IDetailManager.TYPENAME_PROMO);
		json.addProperty(FIELD_NAME, name.getName());
		return json;
	}
}
