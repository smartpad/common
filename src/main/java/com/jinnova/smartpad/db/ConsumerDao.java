package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class ConsumerDao implements DbPopulator<String> {
	
	@Override
	public void preparePopulating() {
		//nothing to do
	}
	
	@Override
	public String populate(ResultSet rs) throws SQLException {
		return rs.getString("consumer_id");
	}

	public DbIterator<String> iterateConsumers() throws SQLException {
		
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		String sql = "select * from consumers";
		System.out.println("SQL: " + sql);
		ResultSet rs = stmt.executeQuery(sql);
		return new DbIterator<String>(conn, stmt, rs, this);
	}
}
