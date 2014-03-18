package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jinnova.smartpad.partner.Branch;
import com.jinnova.smartpad.partner.IBranch;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class BranchDao implements IBranchDao {

	void createBranch(String branchId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into branch (branch_id) values (?)");
			ps.setString(1, branchId);
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
	
	/**
	 * never update id
	 * 
	 * @param branch
	 * @throws SQLException
	 */
	@Override
	public void updateBranch(IBranch branch) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update branch set name=? where branch_id=?");
			ps.setString(1, branch.getName());
			ps.setString(2, branch.getId());
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

	@Override
	public IBranch loadBranch(String branchId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from branch where branch_id = ?");
			ps.setString(1, branchId);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			Branch b = new Branch(branchId);
			b.setName(rs.getString("name"));
			return b;
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
