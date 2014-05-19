package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.member.IMember;
import com.jinnova.smartpad.member.Member;
import com.jinnova.smartpad.partner.IMemberSort;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class MemberDao {

	public int count(String branchId, String operId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from members where branch_id = ? and oper_id = ?");
			ps.setString(1, branchId);
			ps.setString(2, operId);
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

	public LinkedList<IMember> load(String branchId, String operationId, 
			int offset, int pageSize, IMemberSort sortField, boolean ascending) throws SQLException {
		
		String fieldName;
		if (sortField == IMemberSort.Creation) {
			fieldName = "create_date";
		} else if (sortField == IMemberSort.MCardLevel) {
			fieldName = "level";
		} else {
			fieldName = null;
		}
		String orderLimitClause = DaoSupport.buildOrderLimit(fieldName, ascending, offset, pageSize);
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from members where branch_id = ? and oper_id = ? " + orderLimitClause);
			ps.setString(1, branchId);
			ps.setString(1, operationId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<IMember> memberList = new LinkedList<IMember>();
			while (rs.next()) {
				memberList.add(populate(rs));
			}
			return memberList;
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
	
	private static Member populate(ResultSet rs) throws SQLException {
		Member m = new Member(rs.getString("member_id"));
		DaoSupport.populateRecinfo(rs, m.getRecordInfo());
		return m;
	}

	public void insert(String memberId, String operationId, String branchId, IMember t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into members set member_id=?, oper_id=?, branch_id=?, name=?, " +
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.DESC_FIELDS);
			int i = 1;
			ps.setString(i++, memberId);
			ps.setString(i++, operationId);
			ps.setString(i++, branchId);
			ps.setString(i++, t.getName());
			i = DaoSupport.setRecinfoFields(ps, t.getRecordInfo(), i);
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

	public void update(String memberId, IMember t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update members set name=?, " + DaoSupport.RECINFO_FIELDS + ", " + 
					DaoSupport.DESC_FIELDS + " where member_id=?");
			int i = 1;
			ps.setString(i++, t.getName());
			i = DaoSupport.setRecinfoFields(ps, t.getRecordInfo(), i);
			ps.setString(i++, memberId);
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

	public void delete(String memberId, IMember t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete members where member_id=?");
			ps.setString(1, memberId);
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
