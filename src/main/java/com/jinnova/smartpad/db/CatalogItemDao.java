package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class CatalogItemDao {

	public int countCatalogItems(String catalogId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from catalog_items where catalog_id = ?");
			ps.setString(1, catalogId);
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

	public LinkedList<ICatalogItem> loadCatalogItems(String catalogId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from catalog_items where catalog_id = ?");
			ps.setString(1, catalogId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<ICatalogItem> catalogItems = new LinkedList<ICatalogItem>();
			while (rs.next()) {
				CatalogItem item = new CatalogItem(rs.getString("item_id"));
				DaoSupport.populateName(rs, item.getName());
				DaoSupport.populateRecinfo(rs, item.getRecordInfo());
				catalogItems.add(item);
			}
			return catalogItems;
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

	public void insert(String branchId, String itemId, String catalogId, CatalogItem item) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into catalog_items set item_id=?, catalog_id=?, branch_id=?, " + 
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS);
			int i = 1;
			ps.setString(i++, itemId);
			ps.setString(i++, catalogId);
			ps.setString(i++, branchId);
			i = DaoSupport.setRecinfoFields(ps, item.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, item.getName(), i);
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

	public void update(String itemId, CatalogItem item) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update catalog_items set " + 
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS + " where item_id=?");
			int i = 1;
			i = DaoSupport.setRecinfoFields(ps, item.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, item.getName(), i);
			ps.setString(i++, itemId);
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

	public void delete(String itemId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete from catalog_items where item_id=?");
			ps.setString(1, itemId);
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
