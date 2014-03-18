package com.jinnova.smartpad.creditcard;

public class CCard {
	
	private CCardType cardType;
	
	private CCardBranch branch;
	
	private CCardIssuer issuer;

	public boolean qualify(CCardType requiredCreditType,
			CCardBranch requiredCreditBranch, CCardIssuer requiredCreditIssuer) {
		
		if (requiredCreditType != null && this.cardType != requiredCreditType) {
			return false;
		}
		if (requiredCreditBranch != null && this.branch != requiredCreditBranch) {
			return false;
		}
		if (requiredCreditIssuer != null && this.issuer != requiredCreditIssuer) {
			return false;
		}
		return false;
	}

}
