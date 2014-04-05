package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.ICatalogField;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.ICatalogItemSort;
import com.jinnova.smartpad.partner.ICatalogSpec;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class CatalogItemDao {
	
	static final String CS = "cs_";

	public int countCatalogItems(Catalog catalog) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from " + CS + spec.getSpecId() + " where catalog_id = ?");
			ps.setString(1, catalog.getId());
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

	public LinkedList<ICatalogItem> loadCatalogItems(Catalog catalog, int offset,
			int pageSize, ICatalogItemSort sortField, boolean ascending) throws SQLException {
		
		String fieldName;
		if (sortField == ICatalogItemSort.createBy) {
			fieldName = "create_by";
		} else if (sortField == ICatalogItemSort.createDate) {
			fieldName = "create_date";
		} else if (sortField == ICatalogItemSort.name) {
			fieldName = "name";
		} else if (sortField == ICatalogItemSort.updateBy) {
			fieldName = "update_by";
		} else if (sortField == ICatalogItemSort.updateDate) {
			fieldName = "update_date";
		} else {
			fieldName = null;
		}
		String orderLimitClause = DaoSupport.buildOrderLimit(fieldName, ascending, offset, pageSize);
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			
			ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from " + CS + spec.getSpecId() + " where catalog_id = ? " + orderLimitClause);
			ps.setString(1, catalog.getId());
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<ICatalogItem> catalogItems = new LinkedList<ICatalogItem>();
			while (rs.next()) {
				CatalogItem item = new CatalogItem(catalog, rs.getString("item_id"));
				for (ICatalogField field : spec.getAllFields()) {
					item.setField(field.getId(), rs.getString(field.getId()));
				}
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
			ICatalogSpec spec = item.catalog.getSystemCatalog().getCatalogSpec();
			ps = conn.prepareStatement("insert into " + CS + spec.getSpecId() + " set item_id=?, catalog_id=?, branch_id=?, " + 
					DaoSupport.RECINFO_FIELDS + ", " + genSpecFields(spec));
			int i = 1;
			ps.setString(i++, itemId);
			ps.setString(i++, catalogId);
			ps.setString(i++, branchId);
			i = DaoSupport.setRecinfoFields(ps, item.getRecordInfo(), i);
			i = setSpecFields(spec, item, ps, i);
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

	private static String genSpecFields(ICatalogSpec spec) {
		StringBuffer buffer = null;
		for (ICatalogField field : spec.getAllFields()) {
			if (buffer == null) {
				buffer = new StringBuffer();
			} else {
				buffer.append(", ");
			}
			buffer.append(field.getId() + "=?");
		}
		return buffer.toString();
	}

	private static int setSpecFields(ICatalogSpec spec, CatalogItem item, PreparedStatement ps, int i) throws SQLException {
		for (ICatalogField field : spec.getAllFields()) {
			ps.setString(i++, item.getFieldValue(field.getId()));
		}
		return i;
	}

	public void update(String itemId, CatalogItem item) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			ICatalogSpec spec = item.catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update " + CS + spec.getSpecId() + " set " + 
					DaoSupport.RECINFO_FIELDS + ", " + genSpecFields(spec) + " where item_id=?");
			int i = 1;
			i = DaoSupport.setRecinfoFields(ps, item.getRecordInfo(), i);
			i = setSpecFields(spec, item, ps, i);
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
