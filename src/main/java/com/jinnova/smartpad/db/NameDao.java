package com.jinnova.smartpad.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.partner.StringArrayUtils;

class NameDao {

	static final String FIELDS = "name=?, descript=?, images=?";
	
	static int setFields(PreparedStatement ps, IName name, int i) throws SQLException {
		ps.setString(i++, name.getName());
		ps.setString(i++, name.getDescription());
		ps.setString(i++, StringArrayUtils.stringArrayToJson(name.getImages()));
		return i;
	}
	
	static void populate(ResultSet rs, IName name) throws SQLException {
		name.setName(rs.getString("name"));
		name.setDescription(rs.getString("descript"));
		name.setImages(StringArrayUtils.stringArrayFromJson(rs.getString("images")));
	}
}
