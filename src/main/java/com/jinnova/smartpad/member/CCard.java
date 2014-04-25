package com.jinnova.smartpad.member;

public class CCard {
	
	private CCardType cardType;
	
	private CCardBranch branch;
	
	private String issuer;

	public boolean qualify(CCardType requiredCreditType,
			CCardBranch requiredCreditBranch, String requiredCreditIssuer) {
		
		if (requiredCreditType != null && this.cardType != requiredCreditType) {
			return false;
		}
		if (requiredCreditBranch != null && this.branch != requiredCreditBranch) {
			return false;
		}
		if (requiredCreditIssuer != null && !this.issuer.equals(requiredCreditIssuer)) {
			return false;
		}
		return true;
	}

}
