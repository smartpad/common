package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IUser;
import com.jinnova.smartpad.partner.IUserSort;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;
import com.jinnova.smartpad.partner.User;

public class UserDao {

	public void clearDatabaseForTests() throws SQLException {

		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("delete from sp_users");
		stmt.executeUpdate("delete from operations");
		stmt.executeUpdate("delete from catalogs");
		stmt.executeUpdate("delete from promos");
		stmt.executeUpdate("drop table if exists cs_foods");
		stmt.close();
		conn.close();
	}
	
	public void createUser(String branchId, User u) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into sp_users set login=?, passhash=?, branch_id=?, " + DaoSupport.RECINFO_FIELDS);
			int i = 1;
			ps.setString(i++, u.getLogin());
			ps.setString(i++, u.getPasshash());
			ps.setString(i++, branchId);
			i = DaoSupport.setRecinfoFields(ps, u.getRecordInfo(), i);
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
	
	/**
	 * update passpharse only, 
	 * 
	 * @param user
	 * @throws SQLException
	 */
	public void updateUser(IUser user) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update sp_users set passhash=?, " + DaoSupport.RECINFO_FIELDS + " where login=?");
			User u = (User) user;
			int i = 1;
			ps.setString(i++, u.getPasshash());
			i = DaoSupport.setRecinfoFields(ps, user.getRecordInfo(), i);
			ps.setString(i++, u.getLogin());
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

	public void deleteUser(IUser u) throws SQLException {
		
		//not delete primary user
		if (u.isPrimary()) {
			/*((User) u).setPasshash("");
			updateUser(u);
			return;*/
			throw new RuntimeException("Can't delete primary user");
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete from sp_users login=?");
			ps.setString(1, u.getLogin());
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
	
	private static User populateUser(ResultSet rs) throws SQLException {
		User user = new User(rs.getString("login"), /*rs.getString("branch_id"),*/ rs.getString("passhash"));
		return user;
	}

	public IUser loadUser(String login, String[] branchId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from sp_users where login = ?");
			ps.setString(1, login);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			IUser user = populateUser(rs);
			branchId[0] = rs.getString("branch_id");
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

	public LinkedList<IUser> listUsers(String branchId, int offset, int pageSize, IUserSort sortField, boolean ascending) throws SQLException {
		
		String fieldName;
		if (sortField == IUserSort.creation) {
			fieldName = "create_date";
		} else if (sortField == IUserSort.lastUpdate) {
			fieldName = "update_date";
		} else if (sortField == IUserSort.login) {
			fieldName = "login";
		} else {
			fieldName = null;
		}
		String orderLimitClause = DaoSupport.buildOrderLimit(fieldName, ascending, offset, pageSize);
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from sp_users where branch_id = ? " + orderLimitClause);
			ps.setString(1, branchId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<IUser> userList = new LinkedList<IUser>();
			while (rs.next()) {
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

	public int count(String branchId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from sp_users where branch_id = ?");
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

}
