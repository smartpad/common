package com.jinnova.smartpad.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.IRecordInfo;
import com.jinnova.smartpad.partner.StringArrayUtils;

class DaoSupport {

	static final String NAME_FIELDS = "name=?, descript=?, images=?";
	
	static int setNameFields(PreparedStatement ps, IName name, int i) throws SQLException {
		ps.setString(i++, name.getName());
		ps.setString(i++, name.getDescription());
		ps.setString(i++, StringArrayUtils.stringArrayToJson(name.getImages()));
		return i;
	}
	
	static void populateName(ResultSet rs, IName name) throws SQLException {
		name.setName(rs.getString("name"));
		name.setDescription(rs.getString("descript"));
		name.setImages(StringArrayUtils.stringArrayFromJson(rs.getString("images")));
	}

	static final String RECINFO_FIELDS = "create_date=?, update_date=?, create_by=?, update_by=?";
	
	static int setRecinfoFields(PreparedStatement ps, IRecordInfo recinfo, int i) throws SQLException {
		
		ps.setTimestamp(i++, new Timestamp(recinfo.getCreateDate().getTime()));
		Date updateDate = recinfo.getUpdateDate();
		Timestamp updateTimestamp;
		if (updateDate != null) {
			updateTimestamp = new Timestamp(updateDate.getTime());
		} else {
			updateTimestamp = null;
		}
		ps.setTimestamp(i++, updateTimestamp);
		ps.setString(i++, recinfo.getCreateBy());
		ps.setString(i++, recinfo.getUpdateBy());
		return i;
	}
	
	static void populateRecinfo(ResultSet rs, IRecordInfo recordInfo) throws SQLException {
		RecordInfo recinfo = (RecordInfo) recordInfo;
		recinfo.setCreateDate(rs.getTimestamp("create_date"));
		recinfo.setUpdateDate(rs.getTimestamp("update_date"));
		recinfo.setCreateBy(rs.getString("create_by"));
		recinfo.setUpdateBy(rs.getString("update_by"));
	}

	static String buildOrderLimit(String fieldName, boolean ascending, int offset, int pageSize) {
		String direction;
		if (ascending) {
			direction = " asc";
		} else {
			direction = " desc";
		}
		return buildOrderLimit(new String[] {fieldName + direction}, offset, pageSize);
	}

	static String buildOrderLimit(String[] fieldNameAndDirections, int offset, int pageSize) {
		/*if (fieldName == null || "".equals(fieldName.trim())) {
			throw new RuntimeException("Sort field name unset");
		}*/
		String clause = "";
		if (fieldNameAndDirections != null && fieldNameAndDirections.length > 0) {
			for (int i = 0; i < fieldNameAndDirections.length; i++) {
				clause += fieldNameAndDirections[i] + " ";
			}
			clause = "order by " + clause;
		}
		if (pageSize > 0) {
			return clause +=  " limit " + pageSize + " offset " + offset;
		} else {
			return clause;
		}
	}

	static String buildLimit(int offset, int pageSize) {
		if (pageSize > 0) {
			return "limit " + pageSize + " offset " + offset;
		} else {
			return "";
		}
	}
	
	static final String GPS_FIELDS = "gps_lon=?, gps_lat=?, gps_inherit=?";

	static void populateGps(ResultSet rs, GPSInfo gps) throws SQLException {
		gps.setLongitude(rs.getBigDecimal("gps_lon"));
		gps.setLatitude(rs.getBigDecimal("gps_lat"));
		
		//must be after setting gps longitude/latitude
		gps.setInheritFrom(rs.getString("gps_inherit"));
		gps.clearModifiedFlag();
	}

	static int setGpsFields(PreparedStatement ps, GPSInfo gps, int i) throws SQLException {
		ps.setBigDecimal(i++, gps.getLongitude());
		ps.setBigDecimal(i++, gps.getLatitude());
		ps.setString(i++, gps.getInheritFrom());
		gps.clearModifiedFlag();
		return i;
	}

	static String buildConditionIfNotNull(String field, String operator, String value) {
		if (value == null) {
			return "";
		}
		return field + operator + "'" + value + "'";
	}
	
	static String buildDGradeField(BigDecimal lon, BigDecimal lat) {
		String lonS = lon == null ? "null" : lon.toPlainString();
		String latS = lat == null ? "null" : lat.toPlainString();
		return "sp_dist_grade(gps_lon, gps_lat, " + lonS + ", " + latS + ")";
	}
}
