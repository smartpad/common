package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class OperationDao {
	
	private Operation populateOperation(ResultSet rs) throws SQLException {
		Operation oper = new Operation(rs.getString("branch_id"), rs.getString("store_id"), true);
		oper.setName(rs.getString("name"));
		return oper;
	}

	public IOperation loadOperation(String branchId, String storeId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from operations where branch_id = ? and store_id = ?");
			ps.setString(1, branchId);
			ps.setString(2, storeId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			return populateOperation(rs);
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

	public void createOperation(Operation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into operations set branch_id = ?, store_id = ?, " + OP_FIELDS);
			Operation op = (Operation) operation;
			int i = 1;
			ps.setString(i, op.branchId);
			i++;
			ps.setString(i, op.storeId);
			i++;
			setValues(i, op, ps);
			System.out.println("SQL: " + ps);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void updateOperation(IOperation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update operations set " + OP_FIELDS + " where branch_id = ? and store_id = ?");
			Operation op = (Operation) operation;
			int i = 1;
			i = setValues(i, op, ps);
			ps.setString(i, op.branchId);
			i++;
			ps.setString(i, op.storeId);
			i++;
			System.out.println("SQL: " + ps);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	private static final String OP_FIELDS = "name=?";
	
	private static int setValues(int i, Operation op, PreparedStatement ps) throws SQLException {
		ps.setString(i, op.getName());
		i++;
		return i;
	}

}
