package com.jinnova.smartpad.partner;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

public class SmartpadConnectionPool {
	
	public static SmartpadConnectionPool instance;
	
	public final DataSource dataSource;

    private SmartpadConnectionPool(String connectURI) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername("scott");
        ds.setPassword("tiger");
        ds.setUrl(connectURI);
        dataSource = ds;
    }
    
    public static void initialize(String connectURI) {
    	instance = new SmartpadConnectionPool(connectURI);
    }
}