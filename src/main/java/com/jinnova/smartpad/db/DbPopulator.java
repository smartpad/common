package com.jinnova.smartpad.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DbPopulator<T> {
	
	T populate(ResultSet rs) throws SQLException;
	void preparePopulating();

}
