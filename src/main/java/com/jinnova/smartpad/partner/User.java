package com.jinnova.smartpad.partner;

public class User {

	private String login;
	
	private String passhash;

	private Branch branch;

	public User(String login) {
		super();
		this.login = login;
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
}
