package com.jinnova.smartpad.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.google.gson.JsonParser;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.IOperationSort;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;
import com.jinnova.smartpad.partner.StringArrayUtils;

public class OperationDao implements DbPopulator<Operation> {
	
	private boolean populateBranch;
	
	private JsonParser parser;
	
	@Override
	public void preparePopulating() {
		if (parser == null) {
			parser = new JsonParser();
		}
	}
	
	@Override
	public Operation populate(ResultSet rs) throws SQLException {
		Operation oper = new Operation(rs.getString("store_id"), rs.getString("branch_id"), rs.getString("syscat_id"),
				rs.getBigDecimal("gps_lon"), rs.getBigDecimal("gps_lat"), rs.getString("gps_inherit"), populateBranch);
		DaoSupport.populateName(rs, oper.getName());
		DaoSupport.populateRecinfo(rs, oper.getRecordInfo());
		//DaoSupport.populateGps(rs, oper.gps);
		//oper.getOpenHours().setText(rs.getString("open_text"));
		String scheduleJson = rs.getString("open_hours");
		if (scheduleJson != null) {
			oper.getOpenHours().readJson(parser.parse(scheduleJson).getAsJsonObject());
		}
		oper.setMemberLevels(StringArrayUtils.stringArrayFromJson(rs.getString("member_levels")));
		return oper;
	}

	public Operation loadBranch(String branchId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from operations where store_id = ?");
			ps.setString(1, branchId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			parser = new JsonParser();
			populateBranch = true;
			return populate(rs);
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

	public Operation loadStore(String storeId) throws SQLException {
		
		return loadBranch(storeId);
	}

	public LinkedList<IOperation> loadStores(String branchId, int offset, int pageSize, 
			IOperationSort sortField, boolean ascending) throws SQLException {

		String fieldName;
		if (sortField == IOperationSort.creation) {
			fieldName = "create_date";
		} else if (sortField == IOperationSort.lastUpdate) {
			fieldName = "update_date";
		} else if (sortField == IOperationSort.name) {
			fieldName = "name";
		} else {
			fieldName = null;
		}
		String orderLimitClause = DaoSupport.buildOrderLimit(fieldName, ascending, offset, pageSize);
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from operations where branch_id = ? and store_id != branch_id " + orderLimitClause);
			ps.setString(1, branchId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<IOperation> opList = new LinkedList<IOperation>();
			populateBranch = false;
			parser = new JsonParser();
			while (rs.next()) {
				opList.add(populate(rs));
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

	public int countStores(String branchId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from operations where branch_id = ? and store_id != branch_id");
			ps.setString(1, branchId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				return 0;
			}
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

	public void createOperation(String operId, String branchId, Operation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into operations set branch_id=?, store_id=?, " + 
					DaoSupport.NAME_FIELDS + ", " + DaoSupport.RECINFO_FIELDS + ", " + OP_FIELDS);
			Operation op = (Operation) operation;
			int i = 1;
			ps.setString(i++, branchId);
			ps.setString(i++, operId);
			i = DaoSupport.setNameFields(ps, operation.getName(), i);
			i = DaoSupport.setRecinfoFields(ps, operation.getRecordInfo(), i);
			setFields(i, op, ps);
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

	public void updateOperation(String operId, IOperation operation) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update operations set " + DaoSupport.NAME_FIELDS + ", " + 
					DaoSupport.RECINFO_FIELDS + ", " + OP_FIELDS + " where store_id = ?");
			Operation op = (Operation) operation;
			int i = 1;
			i = DaoSupport.setNameFields(ps, operation.getName(), i);
			i = DaoSupport.setRecinfoFields(ps, operation.getRecordInfo(), i);
			i = setFields(i, op, ps);
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
	
	//TODO update store syscat / promotion syscat on changing in branch/store
	private static final String OP_FIELDS = "syscat_id=?, open_hours=?, member_levels=?, " + DaoSupport.GPS_FIELDS;
	
	private static int setFields(int i, Operation op, PreparedStatement ps) throws SQLException {
		ps.setString(i++, ((Catalog) op.getRootCatalog()).getSystemCatalogId());
		ps.setString(i++, op.getOpenHours().writeJson().toString());
		ps.setString(i++, StringArrayUtils.stringArrayToJson(op.getMemberLevels()));
		i = DaoSupport.setGpsFields(ps, op.gps, i);
		return i;
	}

	public void deleteOperation(String operId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete operations where where store_id = ?");
			int i = 1;
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

	public DbIterator<Operation> iterateStores(String branchId, String excludeStoreId, int offset, int size) throws SQLException {
		
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		StringBuffer sql = new StringBuffer();
		sql.append("select * from operations where store_id != branch_id and branch_id = '" + branchId + "' ");
		if (excludeStoreId != null) {
			sql.append("and store_id != '" + excludeStoreId + "'");
		}
		sql.append(DaoSupport.buildLimit(offset, size));
		Statement stmt = conn.createStatement();
		System.out.println("SQL: " + sql.toString());
		ResultSet rs = stmt.executeQuery(sql.toString());
		return new DbIterator<Operation>(conn, stmt, rs, this);
	}

	public DbIterator<Operation> iterateBranchesBySyscatDirectly(String targetSyscatId, String excludeBranchId) throws SQLException {
		
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		StringBuffer sql = new StringBuffer("select * from operations where store_id = branch_id and syscat_id = '" + targetSyscatId + "'");
		if (excludeBranchId != null) {
			sql.append(" and branch_id != '" + excludeBranchId + "'");
		}
		System.out.println("SQL: " + sql.toString());
		ResultSet rs = stmt.executeQuery(sql.toString());
		return new DbIterator<Operation>(conn, stmt, rs, this);
	}

	public DbIterator<Operation> iterateBranchesBySyscatRecursively(String targetSyscatId, String excludeBranchId) throws SQLException {
		
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		StringBuffer sql = new StringBuffer("select * from operations where store_id = branch_id and syscat_id like '" + targetSyscatId + "'");
		if (excludeBranchId != null) {
			sql.append(" and branch_id != '" + excludeBranchId + "'");
		}
		System.out.println("SQL: " + sql.toString());
		ResultSet rs = stmt.executeQuery(sql.toString());
		return new DbIterator<Operation>(conn, stmt, rs, this);
	}

	public void updateBranchGps(String branchId, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update operations set gps_lon=?, gps_lat=? where gps_inherit='" + GPSInfo.INHERIT_BRANCH + "' and branch_id=?");
			int i = 1;
			ps.setBigDecimal(i++, gpsLon);
			ps.setBigDecimal(i++, gpsLat);
			ps.setString(i++, branchId);
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
