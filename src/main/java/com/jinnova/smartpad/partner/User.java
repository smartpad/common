package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.IUser;

public class User implements IUser {

	private String login;
	
	private String passhash;

	private final String branchId;
	//private Branch branch;
	
	private Operation branch;
	
	private LinkedList<Operation> allStores;

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
		if (branch.isPersisted()) {
			new OperationDao().updateOperation(branch);
		} else {
			new OperationDao().createOperation(branch);
		}
	}

	@Override
	public IOperation loadBranch() throws SQLException {
		if (branch != null) {
			return branch;
		}
		branch = (Operation) new OperationDao().loadOperation(branchId, Operation.STORE_MAIN_ID);
		if (branch == null) {
			//branch = new Branch(this.branchId);
			branch = new Operation(this.branchId, Operation.STORE_MAIN_ID, false);
		}
		return branch;
	}
}
