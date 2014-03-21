package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.db.UserDao;
import com.jinnova.smartpad.partner.IUser;

public class User implements IUser {

	private String login;
	
	private String passhash;

	private final String branchId;
	//private Branch branch;
	
	private Operation branch;
	
	private final LinkedList<IOperation> allStores = new LinkedList<IOperation>();
	
	private boolean storesLoaded = false;

	public User(String login, String branchId) {
		super();
		this.login = login;
		this.branchId = branchId;
	}
	
	@Override
	public boolean isPrimary() {
		return this.login.equals(branchId);
	}
	
	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#getLogin()
	 */
	@Override
	public String getLogin() {
		return login;
	}

	public String getPasshash() {
		return passhash;
	}

	/* (non-Javadoc)
	 * @see com.jinnova.smartpad.partner.IUser#setPasshash(java.lang.String)
	 */
	@Override
	public void setPassword(String password) {
		this.passhash = SmartpadCommon.md5(password);
	}
	
	public void setPasshash(String passhash) {
		this.passhash = passhash;
	}

	IUser createUser(String login, String password) throws SQLException {
		if (!isPrimary()) {
			throw new RuntimeException("Unauthorized user");
		}
		User u = new User(login, this.branchId);
		u.setPasshash(SmartpadCommon.md5(password));
		new UserDao().createUser(this.branchId, u);
		return u;
	}
	
	IUser[] listUsers() throws SQLException {
		return new UserDao().listUsers(this.branchId);
	}

	@Override
	public void updateBranch() throws SQLException {
		if (!isPrimary()) {
			return;
		}
		if (branch.isPersisted()) {
			new OperationDao().updateOperation(this.branchId, branch.getOperationId(), branch);
		} else {
			new OperationDao().createOperation(this.branchId, branch.getOperationId(), branch);
			branch.setPersisted(true);
		}
	}

	@Override
	public IOperation loadBranch() throws SQLException {
		if (branch != null) {
			return branch;
		}
		branch = (Operation) new OperationDao().loadOperation(branchId, Operation.STORE_MAIN_ID);
		if (branch == null) {
			//branch = new Branch(this.branchId);
			branch = new Operation(this.branchId, false);
			branch.setOperationId(Operation.STORE_MAIN_ID);
		}
		return branch;
	}

	@Override
	public IOperation[] loadStores() throws SQLException {
		if (!storesLoaded) {
			allStores.clear();
			allStores.addAll(new OperationDao().loadStores(branchId));
			storesLoaded = true;
		}
		return allStores.toArray(new IOperation[allStores.size()]);
	}

	@Override
	public IOperation newStoreInstance() {
		return new Operation(branchId, false);
	}

	@Override
	public void putStore(IOperation store) throws SQLException {
		if (!isPrimary()) {
			return;
		}
		Operation st = (Operation) store;
		if (!st.checkBranch(this.branchId)) {
			throw new RuntimeException("Store does not belong to branch");
		}
		if (st.isPersisted()) {
			new OperationDao().updateOperation(this.branchId, st.getOperationId(), st);
		} else {
			st.setOperationId(SmartpadCommon.md5(st.getName()));
			new OperationDao().createOperation(this.branchId, st.getOperationId(), st);
			allStores.add(st);
			st.setPersisted(true);
		}
	}

	@Override
	public void deleteStore(IOperation store) throws SQLException {
		if (!isPrimary()) {
			return;
		}
		Operation st = (Operation) store;
		new OperationDao().deleteOperation(this.branchId, st.getOperationId());
		allStores.remove(st);
		st.setPersisted(false);
	}
}
