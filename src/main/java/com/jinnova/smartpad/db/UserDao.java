package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IUser;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;
import com.jinnova.smartpad.partner.User;

public class UserDao {
	
	public void createUser(IUser u) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into sp_user (login, passhash, branch_id) values (?, ?, ?)");
			ps.setString(1, u.getLogin());
			ps.setString(2, u.getPasshash());
			ps.setString(3, u.getBranchId());
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
	 * update passpharse only, 
	 * 
	 * @param user
	 * @throws SQLException
	 */
	public void updateUser(IUser u) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update sp_user set passhash=? where login=?");
			ps.setString(1, u.getPasshash());
			ps.setString(2, u.getLogin());
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

	public void deleteUser(IUser u) throws SQLException {
		
		//not delete primary user
		if (u.getLogin().equals(u.getBranchId())) {
			u.setPasshash("");
			updateUser(u);
			return;
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete from sp_user login=?");
			ps.setString(1, u.getLogin());
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
	
	private static User populateUser(ResultSet rs) throws SQLException {
		User user = new User(rs.getString("login"), rs.getString("branch_id"));
		user.setPasshash(rs.getString("passhash"));
		return user;
	}

	public IUser loadUser(String login) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from sp_user where login = ?");
			ps.setString(1, login);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			IUser user = populateUser(rs);
			return user;
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

	public LinkedList<IUser> listUsers(String branchId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from sp_user where branch_id = ?");
			ps.setString(1, branchId);
			rs = ps.executeQuery();
			LinkedList<IUser> userList = new LinkedList<IUser>();
			while (!rs.next()) {
				User user = populateUser(rs);
				userList.add(user);
			}
			return userList;
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

}