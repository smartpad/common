package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.PageMemberMate;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.db.UserDao;
import com.jinnova.smartpad.partner.IUser;

public class User implements IUser {

	private String login;
	
	private String passhash;

	private final String branchId;
	//private Branch branch;
	
	private Operation branch;
	
	//private final LinkedList<IOperation> allStores = new LinkedList<IOperation>();
	private final CachedPagingList<IOperation, IOperationSort> storePagingList;
	
	//private boolean storesLoaded = false;

	public User(String login, String branchId) {
		super();
		this.login = login;
		this.branchId = branchId;
		PageMemberMate<IOperation, IOperationSort> memberMate = new PageMemberMate<IOperation, IOperationSort>() {

			@Override
			public IOperation newMemberInstance() {
				return new Operation(null, User.this.branchId);
			}

			@Override
			public boolean isPersisted(IOperation member) {
				return ((Operation) member).getOperationId() != null;
			}

			@Override
			public LinkedList<IOperation> load(int offset, int pageSize, IOperationSort sortField, boolean ascending) throws SQLException {
				return new OperationDao().loadStores(User.this.branchId); //TODO offset, pagesize, sort
			}

			@Override
			public void insert(IOperation t) throws SQLException {
				Operation op = (Operation) t;
				String newId = SmartpadCommon.md5(User.this.branchId +  op.getName());
				op.setOperationId(newId);
				new OperationDao().createOperation(newId, User.this.branchId, op);
			}

			@Override
			public void update(IOperation t) throws SQLException {
				Operation op = (Operation) t;
				new OperationDao().updateOperation(op.getOperationId(), op);
			}

			@Override
			public void delete(IOperation t) throws SQLException {
				Operation op = (Operation) t;
				new OperationDao().deleteOperation(op.getOperationId());
				op.setOperationId(null);
			}

			@Override
			public int count() throws SQLException {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		Comparator<IOperation> memberComparator = new Comparator<IOperation>() {

			@Override
			public int compare(IOperation o1, IOperation o2) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		this.storePagingList = new CachedPagingList<IOperation, IOperationSort>(memberMate, memberComparator, IOperationSort.creation, true, new IOperation[0]);
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
		if (branch.getOperationId() != null) {
			new OperationDao().updateOperation(branch.getOperationId(), branch);
		} else {
			branch.setOperationId(this.branchId);
			new OperationDao().createOperation(this.branchId, branch.getOperationId(), branch);
			//branch.setPersisted(true);
		}
	}

	@Override
	public IOperation loadBranch() throws SQLException {
		if (branch != null) {
			return branch;
		}
		branch = (Operation) new OperationDao().loadBranch(branchId);
		if (branch == null) {
			//branch = new Branch(this.branchId);
			branch = new Operation(null, this.branchId);
			//branch.setOperationId(Operation.STORE_MAIN_ID);
		}
		return branch;
	}

	@Override
	public IPagingList<IOperation, IOperationSort> getStorePagingList() throws SQLException {
		return storePagingList;
	}

	/*@Override
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
	}*/
}
