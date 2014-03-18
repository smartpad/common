package com.jinnova.smartpad.partner;

public class User {

	private String login;
	
	private String passhash;

	private String branchId;

	public User(String login, String branchId) {
		super();
		this.login = login;
		this.branchId = branchId;
	}
	
	public String getLogin() {
		return login;
	}

	public String getPasshash() {
		return passhash;
	}

	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}
	
	public String getBranchId() {
		return this.branchId;
	}
}
