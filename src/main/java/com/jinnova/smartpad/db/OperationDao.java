package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;
import com.jinnova.smartpad.partner.StringArrayUtils;

public class OperationDao {
	
	private Operation populateOperation(ResultSet rs) throws SQLException {
		Operation oper = new Operation(rs.getString("branch_id"), true);
		oper.setOperationId( rs.getString("store_id"));
		oper.setName(rs.getString("name"));
		oper.getOpenHours().setText(rs.getString("open_text"));
		oper.getOpenHours().fromString(rs.getString("open_hours"));
		oper.setMemberLevels(StringArrayUtils.stringArrayFromJson(rs.getString("member_levels")));
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

	public LinkedList<Operation> loadStores(String branchId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from operations where branch_id = ? and store_id != ?");
			ps.setString(1, branchId);
			ps.setString(2, Operation.STORE_MAIN_ID);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<Operation> opList = new LinkedList<Operation>();
			while (rs.next()) {
				opList.add(populateOperation(rs));
			}
			return opList;
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

	public void createOperation(String branchId, String operId, Operation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into operations set branch_id = ?, store_id = ?, " + OP_FIELDS);
			Operation op = (Operation) operation;
			int i = 1;
			ps.setString(i, branchId);
			i++;
			ps.setString(i, operId);
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

	public void updateOperation(String branchId, String operId, IOperation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update operations set " + OP_FIELDS + " where branch_id = ? and store_id = ?");
			Operation op = (Operation) operation;
			int i = 1;
			i = setValues(i, op, ps);
			ps.setString(i, branchId);
			i++;
			ps.setString(i, operId);
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
	
	private static final String OP_FIELDS = "name=?, open_text=?, open_hours=?, member_levels=?";
	
	private static int setValues(int i, Operation op, PreparedStatement ps) throws SQLException {
		ps.setString(i++, op.getName());
		ps.setString(i++, op.getOpenHours().getText());
		ps.setString(i++, op.getOpenHours().toString());
		ps.setString(i++, StringArrayUtils.stringArrayToJson(op.getMemberLevels()));
		return i;
	}

	public void deleteOperation(String branchId, String operId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete operations where where branch_id = ? and store_id = ?");
			int i = 1;
			ps.setString(i, branchId);
			i++;
			ps.setString(i, operId);
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

}
