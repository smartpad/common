package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogSpec;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogField;
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
			int pageSize, ICatalogSort sortField, boolean ascending, boolean system) throws SQLException {
		
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
			ps = conn.prepareStatement("select * from catalogs where parent_id = ? " + orderLimitClause);
			ps.setString(1, parentId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<ICatalog> subCatalogs = new LinkedList<ICatalog>();
			JsonParser parser = new JsonParser();
			while (rs.next()) {
				Catalog cat = new Catalog(rs.getString("branch_id"), rs.getString("catalog_id"), parentId, rs.getString("syscat_id"));
				DaoSupport.populateName(rs, cat.getName());
				DaoSupport.populateRecinfo(rs, cat.getRecordInfo());
				String spec = rs.getString("spec");
				if (spec != null) {
					JsonObject json = parser.parse(spec).getAsJsonObject();
					((CatalogSpec) cat.getCatalogSpec()).populate(json);
				}
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
		Statement stmt = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into catalogs set catalog_id=?, parent_id=?, branch_id=?, syscat_id=?, spec=?, " + 
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS);
			int i = 1;
			ps.setString(i++, catalogId);
			ps.setString(i++, parentCatalogId);
			ps.setString(i++, branchId);
			ps.setString(i++, cat.getSystemCatalogId());
			CatalogSpec spec = (CatalogSpec) cat.getCatalogSpec();
			ps.setString(i++, spec == null ? null : spec.toJson().toString());
			i = DaoSupport.setRecinfoFields(ps, cat.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, cat.getName(), i);
			System.out.println("SQL: " + ps);
			ps.executeUpdate();
			
			if (spec == null) {
				return;
			}
			String tableName = cat.getCatalogSpec().getSpecId();
			if (tableName == null) {
				throw new RuntimeException("Missing tableName for CatalogSpec");
			}
			StringBuffer tableSql = new StringBuffer();
			tableSql.append("create table " + CatalogItemDao.CS + tableName + " (item_id varchar(32) not null, " +
					"catalog_id varchar(32) NOT NULL, branch_id varchar(32) NOT NULL");
			for (ICatalogField f : cat.getCatalogSpec().getAllFields()) {
				tableSql.append(", ");
				if (f.getId() == null) {
					throw new RuntimeException("Missing columnName for CatalogField");
				}
				tableSql.append(f.getId() + " " + f.getFieldType().sqlType + " default null");
			}
			tableSql.append(", create_date datetime NOT NULL, update_date datetime DEFAULT NULL, create_by varchar(32) NOT NULL, " +
					"update_by varchar(32) DEFAULT NULL, PRIMARY KEY (item_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8");
			System.out.println("SQL: " + tableSql.toString());
			stmt = conn.createStatement();
			stmt.executeUpdate(tableSql.toString());
			
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	public void update(String catalogId, Catalog cat) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update catalogs set syscat_id=?, spec=?, " + DaoSupport.RECINFO_FIELDS + ", " + 
					DaoSupport.NAME_FIELDS + " where catalog_id=?");
			int i = 1;
			ps.setString(i++, cat.getSystemCatalogId());
			CatalogSpec spec = (CatalogSpec) cat.getCatalogSpec();
			ps.setString(i++, spec == null ? null : spec.toJson().toString());
			i = DaoSupport.setRecinfoFields(ps, cat.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, cat.getName(), i);
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
