package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Iterator;

import com.jinnova.smartpad.db.UserDao;

public class PartnerManager implements IPartnerManager {
	
	public static PartnerManager instance;
	
	private PartnerManager() {
		
	}
	
	public static void initialize() {
		SmartpadConnectionPool.initialize("root", "", "jdbc:mysql://localhost/smartpad");
		instance = new PartnerManager();
	}

	public void clearDatabaseForTests() throws SQLException {
		new UserDao().clearDatabaseForTests();
	}
	
	public Iterator<StoreItem> storeItemIterator() {
		return null;
	}
	
	public Iterator<Promotion> promotionIterator() {
		return null;
	}
	
	@Override
	public IUser createPrimaryUser(String login, String password) throws SQLException {
		User u = new User(login, login);
		u.setPasshash(SmartpadCommon.md5(password));
		new UserDao().createUser(u);
		
		//new OperationDao().createOperation(login);
		return u;
	}

	@Override
	public IUser createUser(IUser authorizedUser, String login, String password) throws SQLException {
		if (!authorizedUser.isPrimary()) {
			return null;
		}
		User u = new User(login, ((User) authorizedUser).getBranchId());
		u.setPasshash(SmartpadCommon.md5(password));
		new UserDao().createUser(u);
		return u;
	}

	@Override
	public IUser login(String login, String password) throws SQLException {
		if (password == null || login == null) {
			return null;
		}
		User u = (User) new UserDao().loadUser(login);
		if (u == null) {
			return null;
		}
		if (!SmartpadCommon.md5(password).equals(u.getPasshash())) {
			return null;
		}
		return u;
	}

	@Override
	public void updateUser(IUser authorizedUser, IUser u) throws SQLException {
		if (!authorizedUser.isPrimary() && !authorizedUser.getLogin().equals(u.getLogin())) {
			return;
		}
		new UserDao().updateUser(u);
	}

	@Override
	public void deleteUser(IUser authorizedUser, IUser u) throws SQLException {
		if (!authorizedUser.isPrimary()) {
			return;
		}
		new UserDao().deleteUser(u);
	}

	@Override
	public IUser[] listUsers(IUser authorizedUser) throws SQLException {
		return new UserDao().listUsers(((User) authorizedUser).getBranchId());
	}
}
