package com.jinnova.smartpad.partner;

import java.security.NoSuchAlgorithmException;

public class SampleDataGenerator {

	public static void main(String[] args) throws NoSuchAlgorithmException {
		SmartpadConnectionPool.initialize("");
		PartnerPersistor p = new PartnerPersistor();
		User u = new User("lotte");
		u.setPasshash(PartnerUtils.md5("abc123"));
		p.createUser(u);
	}
}
