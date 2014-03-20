package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class OperationDao {
	
	private Operation populateOperation(ResultSet rs) throws SQLException {
		Operation oper = new Operation(rs.getString("branch_id"), true);
		oper.setStoreId( rs.getString("store_id"));
		oper.setName(rs.getString("name"));
		oper.getOpenHours().setText(rs.getString("open_text"));
		oper.getOpenHours().fromString(rs.getString("open_hours"));
		oper.setMemberLevels(stringArrayFromJson(rs.getString("member_levels")));
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

	public void createOperation(Operation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into operations set branch_id = ?, store_id = ?, " + OP_FIELDS);
			Operation op = (Operation) operation;
			int i = 1;
			ps.setString(i, op.getBranchId());
			i++;
			ps.setString(i, op.getStoreId());
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
			ps.setString(i, op.getBranchId());
			i++;
			ps.setString(i, op.getStoreId());
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
		ps.setString(i++, stringArrayToJson(op.getMemberLevels()));
		return i;
	}

	public void deleteOperation(IOperation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete operations where where branch_id = ? and store_id = ?");
			Operation op = (Operation) operation;
			int i = 1;
			ps.setString(i, op.getBranchId());
			i++;
			ps.setString(i, op.getStoreId());
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
	
	private static String stringArrayToJson(String[] array) {
		JsonArray ja = new JsonArray();
		for (String s : array) {
			ja.add(new JsonPrimitive(s));
		}
		return ja.toString();
	}
	
	private static String[] stringArrayFromJson(String s) {
		JsonParser p = new JsonParser();
		JsonArray ja = p.parse(s).getAsJsonArray();
		String[] array = new String[ja.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = ja.get(i).getAsString();
		}
		return array;
	}

}
