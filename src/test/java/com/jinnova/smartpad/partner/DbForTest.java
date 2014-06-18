package com.jinnova.smartpad.partner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.commons.dbcp2.BasicDataSource;

public class DbForTest extends TestCase {

	public void testDB() throws SQLException {
		BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("root");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:smartpad;MODE=MySQL;MVCC=true");
        
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
		stmt.executeUpdate("CREATE table abc ");
		
		ResultSet rs = stmt.executeQuery("select * from abc");
		assertFalse(rs.next());

		stmt.close();
		conn.close();
        ds.close();
	}
}
