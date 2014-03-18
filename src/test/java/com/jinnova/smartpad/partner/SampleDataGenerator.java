package com.jinnova.smartpad.partner;

import java.sql.SQLException;

public class SampleDataGenerator {

	public static void main(String[] args) throws SQLException {
		PartnerManager.initialize();
		PartnerManager.instance.createPrimaryUser("lotte", "abc123");
	}
}
