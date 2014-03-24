package com.jinnova.smartpad.member;

import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.IRecordInfo;

public class Member implements IMember {
	
	public final String id;
	
	public Member(String id) {
		this.id = id;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPagingList<IPromotion, IPromotionSort> getRelatedPromotions(
			IOperation operation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPagingList<IPromotion, IPromotionSort> getRelatedPromotions(
			ICatalog catalog) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPagingList<IPromotion, IPromotionSort> getRelatedPromotions(
			ICatalogItem catalogItem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMemberCard[] getMemberCards() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICreditCard[] getCreditCards() {
		// TODO Auto-generated method stub
		return null;
	}

}

