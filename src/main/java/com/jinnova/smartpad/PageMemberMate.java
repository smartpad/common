package com.jinnova.smartpad;

import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IUser;

public interface PageMemberMate<T, E> {

	T newMemberInstance(IUser authorizedUser);
	
	boolean isPersisted(T member);

	LinkedList<T> load(IUser authorizedUser, int offset, int pageSize, E sortField, boolean ascending) throws SQLException;
	
	void insert(IUser authorizedUser, T newMember) throws SQLException;
	
	void update(IUser authorizedUser, T member) throws SQLException;
	
	void delete(IUser authorizedUser, T member) throws SQLException;

	int count(IUser authorizedUser) throws SQLException;
}
