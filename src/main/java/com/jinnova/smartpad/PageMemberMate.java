package com.jinnova.smartpad;

import java.sql.SQLException;
import java.util.LinkedList;

public interface PageMemberMate<T, E> {

	T newMemberInstance();
	
	boolean isPersisted(T member);
	
	//E getDefaultSort();
	
	//boolean isDefaultSortAscending();

	LinkedList<T> load(int offset, int pageSize, E sortField, boolean ascending) throws SQLException;
	
	void insert(T t) throws SQLException;
	
	void update(T t) throws SQLException;
	
	void delete(T t) throws SQLException;

	int count() throws SQLException;
}
