package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogField;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.ICatalogItemSort;
import com.jinnova.smartpad.partner.ICatalogSpec;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.PartnerManager;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;
import com.jinnova.smartpad.partner.User;

public class CatalogItemDao implements DbPopulator<CatalogItem> {
	
	static final String CS = "cs_";
	
	private ICatalogSpec spec;
	
	private String syscatId;
	
	/*public CatalogItemDao(ICatalogSpec spec) {
		this.spec = spec;
	}*/

	public int countCatalogItems(String catalogId, ICatalogSpec spec) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from " + CS + spec.getSpecId() + " where catalog_id = ?");
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

	public ICatalogItem loadCatalogItem(String catItemId, ICatalogSpec spec) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			
			//ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			syscatId = spec.getSpecId();
			ps = conn.prepareStatement("select * from " + CS + syscatId + " where item_id = ?");
			ps.setString(1, catItemId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			return populate(rs);
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

	public LinkedList<ICatalogItem> loadCatalogItems(String catalogId, ICatalogSpec spec, int offset,
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
			
			//ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			syscatId = spec.getSpecId();
			ps = conn.prepareStatement("select * from " + CS + syscatId + " where catalog_id = ? " + orderLimitClause);
			ps.setString(1, catalogId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<ICatalogItem> catalogItems = new LinkedList<ICatalogItem>();
			this.spec = spec;
			while (rs.next()) {
				CatalogItem item = populate(rs);
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
	
	@Override
	public CatalogItem populate(ResultSet rs) throws SQLException {
		CatalogItem item = new CatalogItem(rs.getString("branch_id"), rs.getString("store_id"), rs.getString("catalog_id"), syscatId, rs.getString("item_id"));
		DaoSupport.populateGps(rs, item.gps);
		for (ICatalogField field : spec.getAllFields()) {
			item.setField(field.getId(), rs.getString(field.getId()));
		}
		DaoSupport.populateRecinfo(rs, item.getRecordInfo());
		return item;
	}

	public void insert(String itemId, ICatalogSpec spec, CatalogItem item) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			//ICatalogSpec spec = item.catalog.getSystemCatalog().getCatalogSpec();
			ps = conn.prepareStatement("insert into " + CS + spec.getSpecId() + " set item_id=?, catalog_id=?, branch_id=?, store_id=?, " + 
					DaoSupport.GPS_FIELDS + ", " + DaoSupport.RECINFO_FIELDS + ", " + genSpecFields(spec));
			int i = 1;
			ps.setString(i++, itemId);
			ps.setString(i++, item.getCatalogId());
			ps.setString(i++, item.branchId);
			ps.setString(i++, item.storeId);
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

		i = DaoSupport.setGpsFields(ps, item.gps, i);
		i = DaoSupport.setRecinfoFields(ps, item.getRecordInfo(), i);
		for (ICatalogField field : spec.getAllFields()) {
			ps.setString(i++, item.getFieldValue(field.getId()));
		}
		return i;
	}

	public void update(String itemId, ICatalogSpec spec, CatalogItem item) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			//ICatalogSpec spec = item.catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update " + CS + spec.getSpecId() + " set " + DaoSupport.GPS_FIELDS +
					DaoSupport.RECINFO_FIELDS + ", " + genSpecFields(spec) + " where item_id=?");
			int i = 1;
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

	public DbIterator<CatalogItem> iterateCatalogItems(String catalogId, String syscatId, ICatalogSpec spec) throws SQLException {
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		String sql = "select * from " + CS + syscatId + " where catalog_id = '" + catalogId + "'";
		System.out.println("SQL: " + sql);
		ResultSet rs = stmt.executeQuery(sql);
		this.spec = spec;
		return new DbIterator<CatalogItem>(conn, stmt, rs, this);
	}

	public void updateBranchGps(User primaryUser, float gpsLon, float gpsLat) throws SQLException {
		Operation branch = (Operation) primaryUser.getBranch();
		updateGpsByCatalog((Catalog) branch.getRootCatalog(), branch.getBranchId(), gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "branch_id");
		
		primaryUser.getStorePagingList().setPageSize(-1);
		IOperation[] stores = primaryUser.getStorePagingList().loadPage(primaryUser, 1).getPageEntries();
		for (IOperation oneStore : stores) {
			updateGpsByCatalog((Catalog) oneStore.getRootCatalog(), branch.getBranchId(), gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "branch_id");
		}
	}

	public void updateStoreGps(Operation store, float gpsLon, float gpsLat) throws SQLException {
		updateGpsByCatalog((Catalog) store.getRootCatalog(), store.getId(), gpsLon, gpsLat, GPSInfo.INHERIT_STORE, "store_id");
	}
	
	private void updateGpsByCatalog(Catalog rootCat, String targetFieldValue, float gpsLon, float gpsLat, String inherit, String targetField) throws SQLException {
		
		LinkedList<Catalog> catList = new LinkedList<>();
		catList.add(rootCat);
		while (!catList.isEmpty()) {
			Catalog cat = catList.remove();
			updateGps(cat.getSystemCatalogId(), targetFieldValue, gpsLon, gpsLat, inherit, targetField);
			cat.getSubCatalogPagingList().setPageSize(-1);
			ICatalog[] subCats = cat.getSubCatalogPagingList().loadPage(PartnerManager.instance.systemUser, 1).getPageEntries();
			for (ICatalog oneSub : subCats) {
				catList.add((Catalog) oneSub);
			}
		}
	}

	private void updateGps(String syscatId, String targetFieldValue, float gpsLon, float gpsLat, String inherit, String targetField) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update " + CS + syscatId + " set gps_lon=?, gps_lat=? where gps_inherit='" + inherit + "' and " + targetField + "=?");
			int i = 1;
			ps.setFloat(i++, gpsLon);
			ps.setFloat(i++, gpsLat);
			ps.setString(i++, targetFieldValue);
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
