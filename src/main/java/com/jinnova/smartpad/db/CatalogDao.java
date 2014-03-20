package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class CatalogDao {

	public void loadCatalogs(String branchId, String parentId, LinkedList<ICatalog> subCatalogs) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from catalog where parent_id = ?");
			ps.setString(1, parentId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			while (rs.next()) {
				Catalog cat = new Catalog(branchId, rs.getString("catalog_id"), parentId);
				NameDao.populate(rs, cat.getName());
				subCatalogs.add(cat);
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

	public void insert(String branchId, String catalogId, String parentCatalogId, Catalog cat) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into catalog set catalog_id=?, parent_id=?, branch_id=?, " + NameDao.FIELDS);
			int i = 1;
			ps.setString(i++, catalogId);
			ps.setString(i++, parentCatalogId);
			ps.setString(i++, branchId);
			NameDao.setFields(ps, cat.getName(), i);
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

	public void update(String catalogId, Catalog subCat) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update catalog set " + NameDao.FIELDS + " where catalog_id=?");
			int i = 1;
			i = NameDao.setFields(ps, subCat.getName(), i);
			ps.setString(i++, catalogId);
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
