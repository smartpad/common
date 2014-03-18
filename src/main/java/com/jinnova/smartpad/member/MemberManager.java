package com.jinnova.smartpad.member;

import java.util.Iterator;
import java.util.LinkedList;

public class MemberManager {
	
	private LinkedList<CCardIssuer> creditIssuers;
	
	public LinkedList<CCardIssuer> getAllCreditIssuer() {
		return new LinkedList<CCardIssuer>(creditIssuers);
	}

	public Iterator<Consumer> consumerIterator() {
		return null;
	}

}
