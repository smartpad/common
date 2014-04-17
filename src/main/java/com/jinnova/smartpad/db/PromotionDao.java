package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.Promotion;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class PromotionDao implements DbPopulator<Promotion> {

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
	public Promotion populate(ResultSet rs) throws SQLException {
		Promotion promo = new Promotion(rs.getString("promo_id"), rs.getString("store_id"));
		DaoSupport.populateGps(rs, promo.gps);
		DaoSupport.populateRecinfo(rs, promo.getRecordInfo());
		DaoSupport.populateName(rs, promo.getName());
		return promo;
	}

	public void insert(String promotionId, String operationId, String branchId, Promotion t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into promos set promo_id=?, store_id=?, branch_id=?, " +
					DaoSupport.GPS_FIELDS + ", " + DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS);
			int i = 1;
			ps.setString(i++, promotionId);
			ps.setString(i++, operationId);
			ps.setString(i++, branchId);
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
	
	private int setFields(int i, Promotion p, PreparedStatement ps) throws SQLException {
		i = DaoSupport.setGpsFields(ps, p.gps, i);
		i = DaoSupport.setRecinfoFields(ps, p.getRecordInfo(), i);
		i = DaoSupport.setNameFields(ps, p.getName(), i);
		return i;
	}

	public void update(String promotionId, Promotion t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update promos set " + DaoSupport.GPS_FIELDS + ", " + 
					DaoSupport.RECINFO_FIELDS + ", " + DaoSupport.NAME_FIELDS + " where promo_id=?");
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

	public DbIterator<Promotion> iterateOperationPromos(String[] branchIds, int count) throws SQLException { //TODO count
		
		String questionMarks = null;
		for (int i = 0; i < branchIds.length; i++) {
			if (questionMarks == null) {
				questionMarks = "?";
			} else {
				questionMarks += ", ?";
			}
		}
		Connection conn = SmartpadConnectionPool.instance.dataSource.getConnection();
		PreparedStatement ps = conn.prepareStatement("select * from promos where branch_id in (" + questionMarks + ")");
		for (int i = 0; i < branchIds.length; i++) {
			ps.setString(i + 1, branchIds[i]);
		}
		System.out.println("SQL: " + ps);
		ResultSet rs = ps.executeQuery();
		return new DbIterator<Promotion>(conn, ps, rs, this);
	}

	public void updateBranchGps(String branchId, float gpsLon, float gpsLat) throws SQLException {
		updateGps(branchId, gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "branch_id");
	}

	public void updateStoreGps(String storeId, float gpsLon, float gpsLat) throws SQLException {
		updateGps(storeId, gpsLon, gpsLat, GPSInfo.INHERIT_BRANCH, "store_id");
	}

	private void updateGps(String targetFieldValue, float gpsLon, float gpsLat, String inherit, String targetField) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("update promos set gps_lon=?, gps_lat=? where gps_inherit='" + inherit + "' and " + targetField + "=?");
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
