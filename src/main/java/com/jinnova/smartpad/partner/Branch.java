package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.db.OperationDao;

public class Branch implements IBranch {
	
	private String id;
	
	private String name;
	
	private LinkedList<Store> allStores;
	
	public Branch(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public IOperation loadOperation() throws SQLException {
		return new OperationDao().loadOperation(this.id);
	}

	@Override
	public void updateOperation(IOperation operation) throws SQLException {
		new OperationDao().updateOperation(operation);
	}
}
