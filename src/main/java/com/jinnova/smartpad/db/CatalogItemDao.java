package com.jinnova.smartpad.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogField;
import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.CatalogSpec;
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogField;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.ICatalogItemSort;
import com.jinnova.smartpad.partner.ICatalogSpec;
import com.jinnova.smartpad.partner.IDetailManager;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.PartnerManager;
import com.jinnova.smartpad.partner.SmartpadCommon;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;
import com.jinnova.smartpad.partner.User;

public class CatalogItemDao implements DbPopulator<CatalogItem> {
	
	public static final String GROUPING_POSTFIX = "_id";
	//static final String CS = "cs_";
	
	private ICatalogSpec spec;
	
	private boolean popNameDescOnly = false;
	
	//private String syscatId;
	
	/*public CatalogItemDao(ICatalogSpec spec) {
		this.spec = spec;
	}*/

	public int countCatalogItems(String catalogId, String specId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from " + /*CS +*/ specId + " where catalog_id = ?");
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

	public CatalogItem loadCatalogItem(String catItemId, ICatalogSpec spec) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			
			//ICatalogSpec spec = catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			//syscatId = spec.getSpecId();
			this.spec = spec;
			ps = conn.prepareStatement("select * from " + /*CS +*/ spec.getSpecId() + " where item_id = ?");
			ps.setString(1, catItemId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}
			popNameDescOnly = false;
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
			//syscatId = spec.getSpecId();
			ps = conn.prepareStatement("select * from " + /*CS +*/ spec.getSpecId() + " where catalog_id = ? " + orderLimitClause);
			ps.setString(1, catalogId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<ICatalogItem> catalogItems = new LinkedList<ICatalogItem>();
			this.spec = spec;
			popNameDescOnly = false;
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
	public void preparePopulating() {
		//nothign to prepare
	}
	
	@Override
	public CatalogItem populate(ResultSet rs) throws SQLException {
		CatalogItem item = new CatalogItem(rs.getString("branch_id"), rs.getString("store_id"), 
				rs.getString("catalog_id"), rs.getString("syscat_id"), rs.getString("item_id"));
		item.setBranchName(rs.getString("branch_name"));
		item.setCatalogName(rs.getString("cat_name"));
		if (popNameDescOnly) {
			item.setField(ICatalogField.F_NAME, rs.getString(ICatalogField.F_NAME));
			item.setField(ICatalogField.F_DESC, rs.getString(ICatalogField.F_DESC));
			//item.setImages(StringArrayUtils.stringArrayFromJson(rs.getString("images")));
			return item;
		}
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
			ps = conn.prepareStatement("insert into " + /*CS +*/ spec.getSpecId() + 
					" set item_id=?, catalog_id=?, syscat_id=?, branch_id=?, store_id=?, branch_name=?, cat_name=?, " + 
					DaoSupport.GPS_FIELDS + ", " + DaoSupport.RECINFO_FIELDS + ", " + genSpecFields(spec));
			int i = 1;
			ps.setString(i++, itemId);
			ps.setString(i++, item.getCatalogId());
			ps.setString(i++, item.getSyscatId());
			ps.setString(i++, item.getBranchId());
			ps.setString(i++, item.storeId);
			ps.setString(i++, item.getBranchName());
			ps.setString(i++, item.getCatalogName());
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
			
			if (((CatalogField) field).getGroupingType() != ICatalogField.GROUPING_NONE) {
				buffer.append(", " + field.getId() + GROUPING_POSTFIX + "=?");
			}
		}
		return buffer.toString();
	}

	private static int setSpecFields(ICatalogSpec spec, CatalogItem item, PreparedStatement ps, int i) throws SQLException {

		i = DaoSupport.setGpsFields(ps, item.gps, i);
		i = DaoSupport.setRecinfoFields(ps, item.getRecordInfo(), i);
		for (ICatalogField field : spec.getAllFields()) {
			String value = item.getFieldValue(field.getId());
			ps.setString(i++, value);
			
			if (((CatalogField) field).getGroupingType() != ICatalogField.GROUPING_NONE) {
				ps.setString(i++, SmartpadCommon.md5(value));
			}
		}
		return i;
	}
	
	static void createCatalogItemTable(Statement stmt, String tableName, CatalogSpec spec, boolean withClusterColumns) throws SQLException {
		StringBuffer tableSql = new StringBuffer();
		tableSql.append("create table " + /*CatalogItemDao.CS +*/ tableName + "(");
		if (withClusterColumns) {
			tableSql.append("cluster_id int default null, cluster_rank int default null, ");
		}
		tableSql.append("item_id varchar(32) not null, catalog_id varchar(32) NOT NULL, syscat_id varchar(128) not null, " +
				"store_id varchar(32) NOT NULL, branch_id varchar(32) DEFAULT NULL, " +
				"branch_name varchar(2048) DEFAULT NULL, cat_name varchar(2048) NOT NULL, " +
				"gps_lon float DEFAULT NULL, gps_lat float DEFAULT NULL, gps_inherit varchar(8) default null");
		for (ICatalogField f : spec.getAllFields()) {
			tableSql.append(", ");
			if (f.getId() == null) {
				throw new RuntimeException("Missing columnName for CatalogField");
			}
			tableSql.append(f.getId() + " " + f.getFieldType().sqlType + " default null");
			CatalogField cf = (CatalogField) f;
			if (cf.getGroupingType() != ICatalogField.GROUPING_NONE) {
				tableSql.append(", " + f.getId() + GROUPING_POSTFIX + " varchar(32) default null");
			}
		}
		tableSql.append(", create_date datetime NOT NULL, update_date datetime DEFAULT NULL, create_by varchar(32) NOT NULL, " +
				"update_by varchar(32) DEFAULT NULL");
		if (!withClusterColumns) {
			tableSql.append(", PRIMARY KEY (item_id)");
		}
		tableSql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8");
		System.out.println("SQL: " + tableSql.toString());
		stmt.executeUpdate(tableSql.toString());
	}

	public void update(String itemId, ICatalogSpec spec, CatalogItem item) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			//ICatalogSpec spec = item.catalog.getSystemCatalog().getCatalogSpec();
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update " + /*CS +*/ spec.getSpecId() + " set " + DaoSupport.GPS_FIELDS +
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

	public void delete(String itemId, String specId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete from " + specId + " where item_id=?");
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

	/**
	 * @param catId
	 * @param specId
	 * @param excludeItemId
	 * @param segments
	 * @param bySyscat
	 * @param clusterId null if not bySyscat
	 * @param recursive
	 * @param lon
	 * @param lat
	 * @param offset
	 * @param size
	 * @return
	 * @throws SQLException
	 */
	public DbIterator<CatalogItem> iterateItems(String catId, String specId, String excludeItemId, HashMap<String, LinkedList<String>> segments,
			boolean bySyscat, Integer clusterId, boolean recursive, BigDecimal lon, BigDecimal lat, int offset, int size) throws SQLException {
		
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		String catField, fromTable;
		if (bySyscat) {
			catField = "syscat_id";
			fromTable = IDetailManager.CLUSPRE + specId;
		} else {
			catField = "catalog_id";
			fromTable = specId;
		}
		StringBuffer sql = new StringBuffer("select item_id, catalog_id, syscat_id, store_id, branch_id, gps_lon, gps_lat, branch_name, cat_name, name, descript, " + 
				DaoSupport.buildDGradeField(lon, lat) + " as dist_grade from " + fromTable + 
				" where " + DaoSupport.buildConditionLike(catField, catId, recursive) +
				DaoSupport.buildConditionIfNotNull(" and item_id", "!=", excludeItemId) +
				DaoSupport.buildConditionIfNotNull(" and cluster_id", "=", clusterId));
		
		if (segments != null) {
			for (Entry<String, LinkedList<String>> entry : segments.entrySet()) {
				String orTerms = null;
				for (String one : entry.getValue()) {
					if (orTerms == null) {
						orTerms = entry.getKey() + GROUPING_POSTFIX + "='" + one + "'";
					} else {
						orTerms = orTerms + " or " + entry.getKey() + GROUPING_POSTFIX + "='" + one + "'";
					}
				}
				sql.append(" and (" + orTerms + ")");
			}
		}
		sql.append(" order by dist_grade asc " + DaoSupport.buildLimit(offset, size));
		System.out.println("SQL: " + sql.toString());
		ResultSet rs = stmt.executeQuery(sql.toString());
		this.spec = PartnerManager.instance.getCatalogSpec(specId);
		this.popNameDescOnly = true;
		return new DbIterator<CatalogItem>(conn, stmt, rs, this);
	}

	/*public DbIterator<CatalogItem> iterateItemsByCatalog(String catId, String specId, String excludeItemId, 
			boolean recursive, BigDecimal lon, BigDecimal lat, int offset, int size) throws SQLException {
		
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		String sql = "select *, " + DaoSupport.buildDGradeField(lon, lat) + " as dist_grade from " + specId + 
				" where " + DaoSupport.buildConditionLike("catalog_id", catId, recursive) +
				DaoSupport.buildConditionIfNotNull(" and item_id", "!=", excludeItemId) + 
				" order by dist_grade asc " + DaoSupport.buildLimit(offset, size);
		System.out.println("SQL: " + sql);
		ResultSet rs = stmt.executeQuery(sql);
		this.spec = PartnerManager.instance.getCatalogSpec(specId);
		return new DbIterator<CatalogItem>(conn, stmt, rs, this);
	}*/

	public void updateBranchGps(User primaryUser, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		Operation branch = (Operation) primaryUser.getBranch();
		updateGpsByCatalog((Catalog) branch.getRootCatalog(), branch.getBranchId(), gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "branch_id");
		
		primaryUser.getStorePagingList().setPageSize(-1);
		IOperation[] stores = primaryUser.getStorePagingList().loadPage(primaryUser, 1).getPageEntries();
		for (IOperation oneStore : stores) {
			updateGpsByCatalog((Catalog) oneStore.getRootCatalog(), branch.getBranchId(), gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "branch_id");
		}
	}

	public void updateStoreGps(Operation store, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		updateGpsByCatalog((Catalog) store.getRootCatalog(), store.getId(), gpsLon, gpsLat, GPSInfo.INHERIT_STORE, "store_id");
	}
	
	private void updateGpsByCatalog(Catalog rootCat, String targetFieldValue, BigDecimal gpsLon, BigDecimal gpsLat, String inherit, String targetField) throws SQLException {
		
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

	private void updateGps(String syscatId, String targetFieldValue, BigDecimal gpsLon, BigDecimal gpsLat, String inherit, String targetField) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update " + /*CS +*/ syscatId + " set gps_lon=?, gps_lat=? where gps_inherit='" + inherit + "' and " + targetField + "=?");
			int i = 1;
			ps.setBigDecimal(i++, gpsLon);
			ps.setBigDecimal(i++, gpsLat);
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
