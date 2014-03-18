package com.jinnova.smartpad.partner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PartnerPersistor {
	
	public void createUser(User u) throws SQLException {
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement("insert into sp_user (login, passhash, branch_id) values (?, ?, ?)");
		ps.setString(1, u.getLogin());
		ps.setString(2, u.getPasshash());
		ps.setString(3, u.getBranchId());
		ps.executeUpdate();
	}

}
