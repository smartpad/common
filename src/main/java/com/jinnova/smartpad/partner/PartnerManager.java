package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Iterator;

import com.jinnova.smartpad.db.BranchDao;
import com.jinnova.smartpad.db.UserDao;

public class PartnerManager implements IPartnerManager {
	
	public static PartnerManager instance;
	
	private PartnerManager() {
		
	}
	
	public static void initialize() {
		SmartpadConnectionPool.initialize("root", "", "jdbc:mysql://localhost/smartpad");
		instance = new PartnerManager();
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
	
	@Override
	public IUser createPrimaryUser(String login, String password) throws SQLException {
		IUser u = new User(login, login);
		u.setPasshash(PartnerUtils.md5(password));
		new UserDao().createUser(u);
		
		new BranchDao().createBranch(login);
		return u;
	}

	@Override
	public IUser createUser(IUser primaryUser, String login, String password) throws SQLException {
		IUser u = new User(login, primaryUser.getBranchId());
		u.setPasshash(PartnerUtils.md5(password));
		new UserDao().createUser(u);
		return u;
	}

	@Override
	public IUser loadUser(String login) throws SQLException {
		return new UserDao().loadUser(login);
	}

	@Override
	public void updateUser(IUser u) throws SQLException {
		new UserDao().updateUser(u);
	}

	@Override
	public void deleteUser(IUser u) throws SQLException {
		new UserDao().deleteUser(u);
	}

	@Override
	public void updateBranch(IBranch branch) throws SQLException {
		new BranchDao().updateBranch(branch);
	}

	@Override
	public IBranch loadBranch(String branchId) throws SQLException {
		return new BranchDao().loadBranch(branchId);
	}
}
