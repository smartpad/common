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

	public void loadCatalogItems(String catalogId, LinkedList<ICatalogItem> catalogItems) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from catalog_items where catalog_id = ?");
			ps.setString(1, catalogId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			while (rs.next()) {
				CatalogItem item = new CatalogItem(rs.getString("item_id"));
				NameDao.populate(rs, item.getName());
				catalogItems.add(item);
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

	public void insert(String branchId, String itemId, String catalogId, CatalogItem item) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into catalog_items set item_id=?, catalog_id=?, branch_id=?, " + NameDao.FIELDS);
			int i = 1;
			ps.setString(i++, itemId);
			ps.setString(i++, catalogId);
			ps.setString(i++, branchId);
			NameDao.setFields(ps, item.getName(), i);
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
			ps = conn.prepareStatement("update catalog_items set " + NameDao.FIELDS + " where item_id=?");
			int i = 1;
			i = NameDao.setFields(ps, item.getName(), i);
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

}
