package com.jinnova.smartpad.member;

import java.util.LinkedList;

public class Consumer {

	private LinkedList<MCard> mcards;
	
	private LinkedList<CCard> ccards;
	
	public boolean qualify(String branchId, String storeId, int requiredLevel, int requiredMemberPoint) {
		if (mcards == null) {
			return false;
		}
		for (MCard c : mcards) {
			if (!branchId.equals(c.getBranchId()) ) {
				continue;
			}
			if (storeId != null && !storeId.equals(c.getStoreId())) {
				continue;
			}
			if (c.getLevel() < requiredLevel) {
				continue;
			}
			if (c.getPoint() < requiredMemberPoint) {
				continue;
			}
			return true;
		}
		return false;
	}
	
	//private static boolean 

	public boolean qualify(CCardType requiredCreditType,
			CCardBranch requiredCreditBranch, String requiredCreditIssuer) {
		
		if (requiredCreditType != null || requiredCreditBranch != null || requiredCreditIssuer != null) {
			if (ccards == null) {
				return false;
			}
			for (CCard card : ccards) {
				if (card.qualify(requiredCreditType, requiredCreditBranch, requiredCreditIssuer)) {
					return true;
				}
			}
		}
		return false;
	}
}
