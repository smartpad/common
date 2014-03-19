package com.jinnova.smartpad.partner;

import java.sql.SQLException;

import com.jinnova.smartpad.db.BranchDao;
import com.jinnova.smartpad.partner.IUser;

public class User implements IUser {

	private String login;
	
	private String passhash;

	private final String branchId;
	private Branch branch;

	public User(String login, String branchId) {
		super();
		this.login = login;
		this.branchId = branchId;
	}
	
	@Override
	public boolean isPrimary() {
		return this.login.equals(branchId);
	}
	
	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#getLogin()
	 */
	@Override
	public String getLogin() {
		return login;
	}

	public String getPasshash() {
		return passhash;
	}

	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#setPasshash(java.lang.String)
	 */
	@Override
	public void setPassword(String password) {
		this.passhash = PartnerUtils.md5(password);
	}
	
	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}
	
	public String getBranchId() {
		return this.branchId;
	}

	@Override
	public void updateBranch() throws SQLException {
		if (!isPrimary()) {
			return;
		}
		new BranchDao().updateBranch(branch);
	}

	@Override
	public IBranch loadBranch() throws SQLException {
		if (branch != null) {
			return branch;
		}
		branch = new BranchDao().loadBranch(branchId);
		if (branch == null) {
			branch = new Branch(this.branchId);
		}
		return branch;
	}
}
