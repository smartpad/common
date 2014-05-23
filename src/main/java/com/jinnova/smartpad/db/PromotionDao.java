package com.jinnova.smartpad.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.google.gson.JsonParser;
import com.jinnova.smartpad.JsonSupport;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.Promotion;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class PromotionDao implements DbPopulator<Promotion> {
	
	private JsonParser parser = new JsonParser();

	public int count(String operationId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from promos where store_id = ?");
			ps.setString(1, operationId);
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

	public Promotion load(String promoId) throws SQLException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from promos where promo_id = ?");
			ps.setString(1, promoId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
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

	public LinkedList<IPromotion> load(String operationId, int offset, int pageSize, IPromotionSort sortField, boolean ascending) throws SQLException {
		
		String fieldName;
		if (sortField == IPromotionSort.creation) {
			fieldName = "create_date";
		} else if (sortField == IPromotionSort.lastUpdate) {
			fieldName = "update_last";
		} else if (sortField == IPromotionSort.name) {
			fieldName = "name";
		} else {
			fieldName = null;
		}
		String orderLimitClause = DaoSupport.buildOrderLimit(fieldName, ascending, offset, pageSize);
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from promos where store_id = ? " + orderLimitClause);
			ps.setString(1, operationId);
			System.out.println("SQL: " + ps);
			rs = ps.executeQuery();
			LinkedList<IPromotion> promoList = new LinkedList<IPromotion>();
			while (rs.next()) {
				Promotion promo = populate(rs);
				promoList.add(promo);
			}
			return promoList;
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
		//parser = new JsonParser();
	}
	
	@Override
	public Promotion populate(ResultSet rs) throws SQLException {
		Promotion promo = new Promotion(rs.getString("promo_id"), rs.getString("branch_id"), rs.getString("store_id"), rs.getString("syscat_id"));
		promo.setName(rs.getString("name"));
		promo.setRequiredMemberLevel(rs.getInt("member_level"));
		promo.setRequiredMemberPoint(rs.getInt("member_point"));
		promo.readCCardOptions(JsonSupport.parseJsonArray(parser, rs.getString("ccard_req")));
		promo.getSchedule().readJson(JsonSupport.parseJsonObject(parser, rs.getString("schedule")));
		
		DaoSupport.populateGps(rs, promo.gps);
		DaoSupport.populateRecinfo(rs, promo.getRecordInfo());
		DaoSupport.populateDesc(rs, (Name) promo.getDesc(), parser);
		return promo;
	}

	public void insert(String promotionId, String branchId, String storeId, String syscatId, Promotion t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into promos set promo_id=?, branch_id=?, store_id=?, syscat_id=?, " +
					FIELDS + ", " + DaoSupport.GPS_FIELDS + ", " + DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.DESC_FIELDS);
			int i = 1;
			ps.setString(i++, promotionId);
			ps.setString(i++, branchId);
			ps.setString(i++, storeId);
			ps.setString(i++, syscatId);
			i = setFields(i, t, ps);
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
	
	private static final String FIELDS =  "name=?, member_level=?, member_point=?, ccard_req=?, schedule=?";
	
	private int setFields(int i, Promotion p, PreparedStatement ps) throws SQLException {
		ps.setString(i++, p.getName());
		ps.setInt(i++, p.getRequiredMemberLevel());
		ps.setInt(i++, p.getRequiredMemberPoint());
		ps.setString(i++, p.getCCardOptionsJson());
		ps.setString(i++, p.getSchedule().writeJson());
		i = DaoSupport.setGpsFields(ps, p.gps, i);
		i = DaoSupport.setRecinfoFields(ps, p.getRecordInfo(), i);
		i = DaoSupport.setDescFields(ps, (Name) p.getDesc(), i);
		return i;
	}

	public void update(String promotionId, Promotion t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update promos set " + FIELDS + ", " + DaoSupport.GPS_FIELDS + ", " + 
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.DESC_FIELDS + " where promo_id=?");
			int i = 1;
			i = setFields(i, t, ps);
			ps.setString(i++, promotionId);
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

	public void delete(String promotionId, IPromotion t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("delete promos where promo_id=?");
			ps.setString(1, promotionId);
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

	public DbIterator<Promotion> iteratePromosByBranch(String[] branchIds, String[] sortFieldAndDirections, int offset, int size) throws SQLException {
		
		String questionMarks = null;
		for (int i = 0; i < branchIds.length; i++) {
			if (questionMarks == null) {
				questionMarks = "?";
			} else {
				questionMarks += ", ?";
			}
		}
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		String sql = "select * from promos where branch_id in (" + questionMarks + ")" + DaoSupport.buildOrderLimit(sortFieldAndDirections, offset, size);
		PreparedStatement ps = conn.prepareStatement(sql);
		for (int i = 0; i < branchIds.length; i++) {
			ps.setString(i + 1, branchIds[i]);
		}
		System.out.println("SQL: " + ps);
		ResultSet rs = ps.executeQuery();
		return new DbIterator<Promotion>(conn, ps, rs, this);
	}

	public DbIterator<Promotion> iteratePromosBySyscat(String syscatId, String excludePromoId, boolean recursive, Integer clusterId) throws SQLException {
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		String tableName = clusterId != null ? "promos_clusters" : "promos";
		String sql = "select * from " + tableName + " where " + DaoSupport.buildConditionLike("syscat_id", syscatId, recursive) +
				DaoSupport.buildConditionIfNotNull(" and cluster_id", "=", clusterId) + DaoSupport.buildConditionIfNotNull(" and promo_id", "!=", excludePromoId);
		Statement stmt = conn.createStatement();
		System.out.println("SQL: " + sql);
		ResultSet rs = stmt.executeQuery(sql);
		return new DbIterator<Promotion>(conn, stmt, rs, this);
	}

	public void updateBranchGps(String branchId, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		updateGps(branchId, gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "branch_id");
	}

	public void updateStoreGps(String storeId, BigDecimal gpsLon, BigDecimal gpsLat) throws SQLException {
		updateGps(storeId, gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "store_id");
	}

	private void updateGps(String targetFieldValue, BigDecimal gpsLon, BigDecimal gpsLat, String inherit, String targetField) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update promos set gps_lon=?, gps_lat=? where gps_inherit='" + inherit + "' and " + targetField + "=?");
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
