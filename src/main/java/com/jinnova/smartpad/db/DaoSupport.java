package com.jinnova.smartpad.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.RecordInfo;
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
		if (fieldName == null || "".equals(fieldName.trim())) {
			throw new RuntimeException("Sort field name unset");
		}
		if (ascending) {
			fieldName += " asc";
		} else {
			fieldName += " desc";
		}
		String clause = "order by " + fieldName;
		if (pageSize > 0) {
			return clause +=  " limit " + pageSize + " offset " + offset;
		} else {
			return clause;
		}
	}
}
