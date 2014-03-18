package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Iterator;

public class PartnerManager {
	
	public static PartnerManager instance;
	
	private PartnerPersistor persistor = new PartnerPersistor();
	
	private PartnerManager() {
		
	}
	
	public static void initialize() {
		SmartpadConnectionPool.initialize("root", "", "jdbc:mysql://localhost/smartpad");
		instance = new PartnerManager();
	}
	
	public User createPrimaryUser(String login, String password) throws SQLException {
		User u = new User(login, login);
		u.setPasshash(PartnerUtils.md5(password));
		persistor.createUser(u);
		return u;
	}
	
	public Iterator<Branch> branchIterator() {
		return null;
	}
	
	public Iterator<StoreItem> storeItemIterator() {
		return null;
	}
	
	public Iterator<Promotion> promotionIterator() {
		return null;
	}
}
