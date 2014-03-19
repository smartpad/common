package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class OperationDao {

	public IOperation loadOperation(String operationId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from operations where oper_id = ?");
			ps.setString(1, operationId);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			Operation oper = new Operation(/*rs.getString("oper_id")*/);
			return oper;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public Object updateOperation(IOperation operation) {
		// TODO Auto-generated method stub
		return null;
	}

}
