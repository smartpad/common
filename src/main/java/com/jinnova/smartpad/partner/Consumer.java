package com.jinnova.smartpad.partner;

import java.util.LinkedList;

import com.jinnova.smartpad.creditcard.CCard;
import com.jinnova.smartpad.member.MCard;
import com.jinnova.smartpad.member.MCardLevel;

public class Consumer {

	private LinkedList<MCard> mcards;
	
	private LinkedList<CCard> ccards;
	
	boolean qualify(MCardLevel level) {
		if (mcards == null) {
			return false;
		}
		for (MCard c : mcards) {
			if (level.qualify(c)) {
				return true;
			}
		}
		return false;
	}
}
