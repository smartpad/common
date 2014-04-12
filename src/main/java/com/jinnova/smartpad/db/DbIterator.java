package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbIterator<T> {
	
	private Connection conn;
	
	private Statement stmt;
	
	private ResultSet rs;
	
	private DbPopulator<T> populator;
	
	public DbIterator(Connection conn, Statement stmt, ResultSet rs, DbPopulator<T> populator) {
		this.conn = conn;
		this.stmt = stmt;
		this.rs = rs;
		this.populator = populator;
	}

	void close() throws SQLException {
		if (rs != null) {
			rs.close();
		}
		if (stmt != null) {
			stmt.close();
		}
		if (conn != null) {
			conn.close();
		}
	}

	public boolean hasNext() throws SQLException {
		if (!rs.next()) {
			return false;
		}
		return true;
	}

	public T next() throws SQLException {
		return populator.populate(rs);
	}
}
