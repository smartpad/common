package com.jinnova.smartpad;

import java.sql.SQLException;
import java.util.LinkedList;

public interface PageMemberMate<T> {

	T newMemberInstance();
	
	boolean isPersisted(T member);

	LinkedList<T> load(int offset, int pageSize) throws SQLException;
	
	void insert(T t) throws SQLException;
	
	void update(T t) throws SQLException;
	
	void delete(T t) throws SQLException;
}
