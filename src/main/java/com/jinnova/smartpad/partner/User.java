package com.jinnova.smartpad.partner;

import com.jinnova.smartpad.partner.IUser;

public class User implements IUser {

	private String login;
	
	private String passhash;

	private String branchId;

	public User(String login, String branchId) {
		super();
		this.login = login;
		this.branchId = branchId;
	}
	
	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#getLogin()
	 */
	public String getLogin() {
		return login;
	}

	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#getPasshash()
	 */
	public String getPasshash() {
		return passhash;
	}

	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#setPasshash(java.lang.String)
	 */
	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}
	
	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#getBranchId()
	 */
	public String getBranchId() {
		return this.branchId;
	}
}
