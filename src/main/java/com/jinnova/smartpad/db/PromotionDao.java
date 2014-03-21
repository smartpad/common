package com.jinnova.smartpad.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.Promotion;
import com.jinnova.smartpad.partner.SmartpadConnectionPool;

public class PromotionDao {

	public int count(String operationId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select count(*) from promos where oper_id = ?");
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

	public LinkedList<IPromotion> load(String operationId, int offset, int pageSize, IPromotionSort sortField, boolean ascending) throws SQLException {
		
		String sortTerm;
		if (sortField == IPromotionSort.creation) {
			sortTerm = "creation";
		} else if (sortField == IPromotionSort.lastUpdate) {
			sortTerm = "update_last";
		} else if (sortField == IPromotionSort.startDate) {
			sortTerm = null;
		} else if (sortField == IPromotionSort.endDate) {
			sortTerm = null;
		} else {
			sortTerm = null;
		}
		
		if (sortTerm != null) {
			if (ascending) {
				sortTerm += " asc";
			} else {
				sortTerm += " desc";
			}
		} else {
			sortTerm = "";
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("select * from promos where oper_id = ? order by " + sortTerm + " limit ? offset ?");
			ps.setString(1, operationId);
			ps.setInt(2, pageSize);
			ps.setInt(3, offset);
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
	
	private static Promotion populate(ResultSet rs) throws SQLException {
		Promotion promo = new Promotion(rs.getString("promo_id"), rs.getString("oper_id"));
		promo.setCreationDate(rs.getTimestamp("creation"));
		promo.setLastUpdate(rs.getTimestamp("update_last"));
		NameDao.populate(rs, promo.getName());
		return promo;
	}

	public void insert(String promotionId, String operationId, String branchId, IPromotion t) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SmartpadConnectionPool.instance.dataSource.getConnection();
			ps = conn.prepareStatement("insert into promos set promo_id=?, oper_id=?, branch_id=?, creation=?, update_last=?, " + NameDao.FIELDS);
			int i = 1;
			ps.setString(i++, promotionId);
			ps.setString(i++, operationId);
			ps.setString(i++, branchId);
			ps.setTimestamp(i++, new Timestamp(t.getCreationDate().getTime()));
			ps.setTimestamp(i++, new Timestamp(t.getLastUpdate().getTime()));
			i = NameDao.setFields(ps, t.getName(), i);
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

	public void update(String promotionId, IPromotion t) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void delete(String promotionId, IPromotion t) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
