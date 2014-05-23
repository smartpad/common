package com.jinnova.smartpad.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.partner.GPSInfo;
import com.jinnova.smartpad.partner.IRecordInfo;

class DaoSupport {

	static final String DESC_FIELDS = "descript=?, images=?";
	
	static int setDescFields(PreparedStatement ps, Name name, int i) throws SQLException {
		//ps.setString(i++, name.getName());
		ps.setString(i++, name.getDescription());
		ps.setString(i++, name.getImagesJson());
		return i;
	}
	
	static void populateDesc(ResultSet rs, Name name, JsonParser parser) throws SQLException {
		//name.setName(rs.getString("name"));
		name.setDescription(rs.getString("descript"));
		String s = rs.getString("images");
		if (s != null) {
			JsonElement je = parser.parse(s);
			if (je != null && !je.isJsonNull() && je.isJsonObject()) {
				name.populate(je.getAsJsonObject());
			}
		}
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
				if ("".equals(clause)) {
					clause = fieldNameAndDirections[i];
				} else {
					clause = clause + ", " + fieldNameAndDirections[i];
				}
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

	static String buildConditionIfNotNull2(String field, String operator, String value) {
		if (value == null) {
			return null;
		}
		return field + operator + "'" + value + "'";
	}

	static String buildConditionIfNotNull(String field, String operator, Integer value) {
		if (value == null) {
			return "";
		}
		return field + operator + value;
	}

	static String buildConditionWithProperNull(String field, String operator, String value) {
		if (value == null) {
			return field + " is null";
		} else {
			return field + operator + "'" + value + "'";
		}
	}

	static String buildConditionLike(String field, String value, boolean like) {
		if (like) {
			return field + " like '" + value + "%'";
		} else {
			return field + " = '" + value + "'";
		}
	}

	static boolean appendTerm(StringBuffer buffer, String operator, String term) {
		if (term == null) {
			return false;
		}
		if (buffer.length() > 0 && operator != null) {
			buffer.append(operator);
		}
		buffer.append(term);
		return true;
	}
	
	static String buildDGradeField(BigDecimal lon, BigDecimal lat) {
		String lonS = lon == null ? "null" : lon.toPlainString();
		String latS = lat == null ? "null" : lat.toPlainString();
		return "sp_dist_grade(gps_lon, gps_lat, " + lonS + ", " + latS + ")";
	} 
}

class Expression {
	
	Object op1, op2;
	String operator;
	
	Expression(Object op1, String operator, Object op2) {
		this.op1 = op1;
		this.op2 = op2;
		this.operator = operator;
	}
	
	String genString() {
		
		if (op1 == null && op2 == null) {
			return null;
		} else if (op1 == null) {
			return genString(op2, false);
		} else if (op2 == null) {
			return genString(op1, false);
		} else {
			return genString(op1, true) + operator + genString(op2, true);
		}
	}
	
	private static String genString(Object op, boolean withBraces) {
		
		if (op instanceof String) {
			return (String) op;
		} else {
			if (withBraces) {
				return "(" + ((Expression) op).genString() + ")";
			} else {
				return ((Expression) op).genString();
			}
		}
	}
}
