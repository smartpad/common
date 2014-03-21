package com.jinnova.smartpad;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;

public interface PageMemberMate<T, E> {

	T newMemberInstance();
	
	boolean isPersisted(T member);
	
	//E getDefaultSort();
	
	//boolean isDefaultSortAscending();
	
	Comparator<T> getComparator(E sortField);

	LinkedList<T> load(int offset, int pageSize, E sortField, boolean ascending) throws SQLException;
	
	void insert(T t) throws SQLException;
	
	void update(T t) throws SQLException;
	
	void delete(T t) throws SQLException;

	int count() throws SQLException;
}
