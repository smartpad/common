package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class CacheDao {
	
	//private static DataSource dataSource;
	
	//private static HashMap<String, String> versions;

    /*public static void initialize(String login, String password, String connectURI) throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername(login);
        ds.setPassword(password);
        ds.setUrl(connectURI);
        dataSource = ds;
        loadVersionInfo();
    }*/
    
	/*public static void initialize() throws SQLException {
    	Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select type_id, version from similars_ver");
			HashMap<String, String> versionsTemp = new HashMap<String, String>();
			while (rs.next()) {
				versionsTemp.put(rs.getString("type_id"), rs.getString("version"));
			}
			versions = versionsTemp;
			
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
    }*/
    
    public static String query(int targetType, String targetId, String gpsZone, int page) throws SQLException {
    	Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select json from similars where target = ?");
			ps.setString(1, /*versions.get(targetType) +*/ targetType + targetId + page);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			return rs.getString("json");
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
    
    public static void put(String json, int targetType, String targetId, String gpsZone, int page) throws SQLException {
    	
    }

}
