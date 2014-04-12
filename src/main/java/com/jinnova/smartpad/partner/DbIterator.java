package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Iterator;

public interface DbIterator<T> extends Iterator<T> {

	void close() throws SQLException;
}
