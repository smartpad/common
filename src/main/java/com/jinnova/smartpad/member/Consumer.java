package com.jinnova.smartpad.member;

import java.util.LinkedList;


public class Consumer {

	private LinkedList<MCard> mcards;
	
	private LinkedList<CCard> ccards;
	
	public boolean qualify(String operationId, int requiredLevel, int requiredMemberPoint) {
		if (mcards == null) {
			return false;
		}
		for (MCard c : mcards) {
			if (operationId.equals(c.getLevel().getOperationId()) ) {
				continue;
			}
			if (c.getLevel().getLevel() < requiredLevel) {
				continue;
			}
			if (c.getPoint() < requiredMemberPoint) {
				continue;
			}
			return true;
		}
		return false;
	}

	public boolean qualify(CCardType requiredCreditType,
			CCardBranch requiredCreditBranch, CCardIssuer requiredCreditIssuer) {
		
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
