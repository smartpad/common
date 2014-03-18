package com.jinnova.smartpad.partner;

import java.util.Iterator;

import com.jinnova.smartpad.db.BranchDao;
import com.jinnova.smartpad.db.IBranchDao;
import com.jinnova.smartpad.db.IUserDao;
import com.jinnova.smartpad.db.UserDao;

public class PartnerManager implements IPartnerManager {
	
	public static PartnerManager instance;
	
	private PartnerManager() {
		
	}
	
	public static void initialize() {
		SmartpadConnectionPool.initialize("root", "", "jdbc:mysql://localhost/smartpad");
		instance = new PartnerManager();
	}

	@Override
	public IUserDao getUserDao() {
		return new UserDao();
	}

	@Override
	public IBranchDao getBranchDao() {
		return new BranchDao();
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
