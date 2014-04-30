package com.jinnova.smartpad.db;

import static com.jinnova.smartpad.partner.IDetailManager.CLUSPRE;

import java.math.BigDecimal;
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
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogField;
import com.jinnova.smartpad.partner.ICatalogSort;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class CatalogDao implements DbPopulator<Catalog> {
	
	private JsonParser specParser;
	
	/*public CatalogDao() {
		this(true);
	}
	
	public CatalogDao(boolean parseSpec) {
		if (parseSpec) {
			parser = new JsonParser();
		}
	}*/

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

	public Catalog loadCatalog(String catId, boolean loadSpec) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from catalogs where catalog_id = ?");
			ps.setString(1, catId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			if (loadSpec) {
				specParser = new JsonParser();
			}
			if (!rs.next()) {
				return null;
			}
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

	public LinkedList<ICatalog> loadSubCatalogs(String parentId, int offset,
			int pageSize, ICatalogSort sortField, boolean ascending, boolean parseSpec) throws SQLException {
		
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
			
			if (parseSpec) {
				specParser = new JsonParser();
			}
			LinkedList<ICatalog> subCatalogs = new LinkedList<ICatalog>();
			while (rs.next()) {
				Catalog cat = populate(rs);
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
	
	@Override
	public void preparePopulating() {
		//nothign to prepare
	}
	
	@Override
	public Catalog populate(ResultSet rs) throws SQLException {
		Catalog cat = new Catalog(rs.getString("branch_id"), rs.getString("store_id"), 
				rs.getString("catalog_id"), rs.getString("parent_id"), rs.getString("syscat_id"));
		DaoSupport.populateGps(rs, cat.gps);
		DaoSupport.populateName(rs, cat.getName());
		DaoSupport.populateRecinfo(rs, cat.getRecordInfo());
		String spec = rs.getString("spec");
		if (specParser != null && spec != null) {
			JsonObject json = specParser.parse(spec).getAsJsonObject();
			((CatalogSpec) cat.getCatalogSpec()).populate(json);
		}
		return cat;
	}

	public void insert(String branchId, String storeId, String catalogIdPrefix, 
			String[] catalogIdGen, String parentCatalogId, Catalog cat, boolean createClusterTable) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		Statement stmtSubcount = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean rollbackable = false;
		boolean success = false;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			conn.setAutoCommit(false);

			stmtSubcount = conn.createStatement();
			String sql = "select max(partial_id) from catalogs where parent_id='" + parentCatalogId + "'";
			System.out.println("SQL: " + sql);
			rs = stmtSubcount.executeQuery(sql);
			int partialId = 1;
			if (rs.next()) {
				partialId = rs.getInt(1);
			}
			partialId++;
			if (catalogIdGen[0] == null) {
				catalogIdGen[0] = catalogIdPrefix + "_" + partialId;
			}
			
			ps = conn.prepareStatement("insert into catalogs set catalog_id=?, partial_id=?, parent_id=?, branch_id=?, store_id=?, syscat_id=?, spec=?, " + 
					DaoSupport.GPS_FIELDS + ", " + DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS);
			rollbackable = true;
			int i = 1;
			ps.setString(i++, catalogIdGen[0]);
			ps.setInt(i++, partialId);
			ps.setString(i++, parentCatalogId);
			ps.setString(i++, branchId);
			ps.setString(i++, storeId);
			ps.setString(i++, cat.getSystemCatalogId());
			CatalogSpec spec = (CatalogSpec) cat.getCatalogSpec();
			ps.setString(i++, spec == null ? null : spec.toJson().toString());
			i = DaoSupport.setGpsFields(ps, cat.gps, i);
			i = DaoSupport.setRecinfoFields(ps, cat.getRecordInfo(), i);
			i = DaoSupport.setNameFields(ps, cat.getName(), i);
			System.out.println("SQL: " + ps);
			ps.executeUpdate();
			
			//update subcount
			/*sql = "update catalogs set sub_count=" + subcount + " where catalog_id='" + parentCatalogId + "'";
			System.out.println("SQL: " + sql);
			stmtSubcount.executeUpdate(sql);*/
			
			if (spec == null) {
				conn.commit();
				success = true;
				return;
			}
			if (spec.getSpecId() == null) {
				throw new RuntimeException("Missing tableName for CatalogSpec");
			}
			stmt = conn.createStatement();
			createCatalogItemTable(stmt, spec.getSpecId(), spec, false);
			if (createClusterTable) {
				createCatalogItemTable(stmt, CLUSPRE + spec.getSpecId(), spec, true);
			}
			conn.commit();
			success = true;
			
		} finally {
			if (rollbackable && !success) {
				conn.rollback();
			}
			if (rs != null) {
				rs.close();
			}
			if (stmtSubcount != null) {
				stmtSubcount.close();
			}
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
	
	private void createCatalogItemTable(Statement stmt, String tableName, CatalogSpec spec, boolean withClusterColumns) throws SQLException {
		StringBuffer tableSql = new StringBuffer();
		tableSql.append("create table " + /*CatalogItemDao.CS +*/ tableName + "(");
		if (withClusterColumns) {
			tableSql.append("cluster_id int default null, cluster_rank int default null, ");
		}
		tableSql.append("item_id varchar(32) not null, catalog_id varchar(32) NOT NULL, syscat_id varchar(128) not null, " +
				"store_id varchar(32) NOT NULL, branch_id varchar(32) NOT NULL, " +
				//"cluster_id varchar(32) default null, cluster_rank int default null, " +
				"gps_lon float DEFAULT NULL, gps_lat float DEFAULT NULL, gps_inherit varchar(8) default null");
		for (ICatalogField f : spec.getAllFields()) {
			tableSql.append(", ");
			if (f.getId() == null) {
				throw new RuntimeException("Missing columnName for CatalogField");
			}
			tableSql.append(f.getId() + " " + f.getFieldType().sqlType + " default null");
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

	public void update(String catalogId, Catalog cat) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update catalogs set syscat_id=?, spec=?, " + DaoSupport.GPS_FIELDS + ", " +
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS + " where catalog_id=?");
			int i = 1;
			ps.setString(i++, cat.getSystemCatalogId());
			CatalogSpec spec = (CatalogSpec) cat.getCatalogSpec();
			ps.setString(i++, spec == null ? null : spec.toJson().toString());
			i = DaoSupport.setGpsFields(ps, cat.gps, i);
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

	public DbIterator<Catalog> iterateCatalogs(String parentCatalogId, String excludeCatId, boolean recursive, int offset, int count) throws SQLException {
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		StringBuffer sql = new StringBuffer("select * from catalogs where " + DaoSupport.buildConditionLike("parent_id", parentCatalogId, recursive));
		sql.append(DaoSupport.buildConditionIfNotNull(" and catalog_id", "!=", excludeCatId));
		sql.append(DaoSupport.buildLimit(offset, count));
		System.out.println("SQL: " + sql.toString());
		ResultSet rs = stmt.executeQuery(sql.toString());
		return new DbIterator<Catalog>(conn, stmt, rs, this);
	}

	public DbIterator<Catalog> iterateCatalogsBySyscat(String syscatId, String excludeCatId, boolean recursive, int offset, int count) throws SQLException {
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		Statement stmt = conn.createStatement();
		StringBuffer sql = new StringBuffer("select * from catalogs where " + DaoSupport.buildConditionLike("syscat_id", syscatId, recursive));
		sql.append(DaoSupport.buildConditionIfNotNull(" and catalog_id", "!=", excludeCatId));
		sql.append(DaoSupport.buildLimit(offset, count));
		System.out.println("SQL: " + sql.toString());
		ResultSet rs = stmt.executeQuery(sql.toString());
		return new DbIterator<Catalog>(conn, stmt, rs, this);
	}

	public void updateBranchGps(String branchId, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		updateGps(branchId, gpsLon, gpsLat, GPSInfo.INHERIT_STORE, "branch_id");
	}

	public void updateStoreGps(String storeId, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		updateGps(storeId, gpsLon, gpsLat, GPSInfo.INHERIT_STORE, "store_id");
	}

	private void updateGps(String targetFieldValue, BigDecimal gpsLon, BigDecimal gpsLat, String inherit, String targetField) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update catalogs set gps_lon=?, gps_lat=? where gps_inherit='" + inherit + "' and " + targetField + "=?");
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
