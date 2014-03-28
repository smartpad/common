package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogSort;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class CatalogDao {

	public int countSubCatalogs(String parentId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from catalogs where parent_id = ?");
			ps.setString(1, parentId);
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

	public LinkedList<ICatalog> loadSubCatalogs(String parentId, int offset,
			int pageSize, ICatalogSort sortField, boolean ascending) throws SQLException {
		
		String fieldName;
		if (sortField == ICatalogSort.createBy) {
			fieldName = "create_by";
		} else if (sortField == ICatalogSort.createDate) {
			fieldName = "create_date";
		} else if (sortField == ICatalogSort.name) {
			fieldName = "name";
		} else if (sortField == ICatalogSort.updateBy) {
			fieldName = "update_by";
		} else if (sortField == ICatalogSort.updateDate) {
			fieldName = "update_date";
		} else {
			fieldName = null;
		}
		String orderLimitClause = DaoSupport.buildOrderLimit(fieldName, ascending, offset, pageSize);
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from catalogs where parent_id = ? and " + orderLimitClause);
			ps.setString(1, parentId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<ICatalog> subCatalogs = new LinkedList<ICatalog>();
			while (rs.next()) {
				Catalog cat = new Catalog(rs.getString("branch_id"), rs.getString("catalog_id"), parentId);
				DaoSupport.populateName(rs, cat.getName());
				DaoSupport.populateRecinfo(rs, cat.getRecordInfo());
				subCatalogs.add(cat);
			}
			return subCatalogs;
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
			ps = conn.prepareStatement("insert into catalogs set catalog_id=?, parent_id=?, branch_id=?, " + 
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS);
			int i = 1;
			ps.setString(i++, catalogId);
			ps.setString(i++, parentCatalogId);
			ps.setString(i++, branchId);
			i = DaoSupport.setRecinfoFields(ps, cat.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, cat.getName(), i);
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
			ps = conn.prepareStatement("update catalogs set " + DaoSupport.RECINFO_FIELDS + ", " + 
					DaoSupport.NAME_FIELDS + " where catalog_id=?");
			int i = 1;
			i = DaoSupport.setRecinfoFields(ps, subCat.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, subCat.getName(), i);
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

	public void delete(String catalogId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete from catalogs where catalog_id=?");
			ps.setString(1, catalogId);
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
