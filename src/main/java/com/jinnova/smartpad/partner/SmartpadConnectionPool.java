package com.jinnova.smartpad.partner;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public class SmartpadConnectionPool {
	
	public static SmartpadConnectionPool instance;
	
	public final DataSource dataSource;

    private SmartpadConnectionPool(String login, String password, String connectURI) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername(login);
        ds.setPassword(password);
        ds.setUrl(connectURI);
        dataSource = ds;
        System.out.println("Connection pool is ready for " + connectURI);
    }
    
    public static void initialize(String login, String password, String connectURI) {
    	instance = new SmartpadConnectionPool(login, password, connectURI);
    }
}