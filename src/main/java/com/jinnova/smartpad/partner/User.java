package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.PageMemberMate;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.RecordInfoHolder;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.IUser;

public class User implements IUser, RecordInfoHolder {

	private String login;
	
	private String loginTemp;
	
	private String passhash;

	private final String branchId;
	
	private Operation branch;
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private final CachedPagingList<IOperation, IOperationSort> storePagingList;

	public User(String login, String branchId, String passhash) {
		super();
		this.login = login;
		this.branchId = branchId;
		this.passhash = passhash;
		@SuppressWarnings("unchecked")
		final Comparator<IOperation>[] comparators = new Comparator[5];
		comparators[IOperationSort.creation.ordinal()] = new Comparator<IOperation>() {
			@Override
			public int compare(IOperation o1, IOperation o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o2.getRecordInfo().getCreateDate());
			}
		};
		comparators[IOperationSort.lastUpdate.ordinal()] = new Comparator<IOperation>() {
			@Override
			public int compare(IOperation o1, IOperation o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		comparators[IOperationSort.name.ordinal()] = new Comparator<IOperation>() {
			@Override
			public int compare(IOperation o1, IOperation o2) {
				return o1.getName().getName().compareToIgnoreCase(o2.getName().getName());
			}
		};
		PageMemberMate<IOperation, IOperationSort> memberMate = new PageMemberMate<IOperation, IOperationSort>() {

			@Override
			public IOperation newMemberInstance(IUser authorizedUser) {
				return new Operation(null, User.this.branchId);
			}

			@Override
			public boolean isPersisted(IOperation member) {
				return ((Operation) member).getOperationId() != null;
			}

			@Override
			public LinkedList<IOperation> load(IUser authorizedUser, 
					int offset, int pageSize, IOperationSort sortField, boolean ascending) throws SQLException {
				
				return new OperationDao().loadStores(User.this.branchId, offset, pageSize, sortField, ascending);
			}

			@Override
			public void insert(IUser authorizedUser, IOperation t) throws SQLException {
				Operation op = (Operation) t;
				String newId = SmartpadCommon.md5(User.this.branchId +  op.getName());
				op.setOperationId(newId);
				new OperationDao().createOperation(newId, User.this.branchId, op);
			}

			@Override
			public void update(IUser authorizedUser, IOperation t) throws SQLException {
				Operation op = (Operation) t;
				new OperationDao().updateOperation(op.getOperationId(), op);
			}

			@Override
			public void delete(IUser authorizedUser, IOperation t) throws SQLException {
				Operation op = (Operation) t;
				new OperationDao().deleteOperation(op.getOperationId());
				op.setOperationId(null);
			}

			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new OperationDao().countStores(User.this.branchId);
			}
		};
		this.storePagingList = new CachedPagingList<IOperation, IOperationSort>(memberMate, comparators, IOperationSort.creation, new IOperation[0]);
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
	
	@Override
	public void setLogin(String login) {
		if (this.login != null) {
			throw new RuntimeException("Login can't be changed");
		}
		this.loginTemp = login;
	}
	
	void setLogin() {
		this.login = this.loginTemp;
	}

	public String getPasshash() {
		return passhash;
	}
	
	String getBranchId() {
		return this.branchId;
	}

	@Override
	public void setPassword(String password) {
		this.passhash = SmartpadCommon.md5(password);
	}

	@Override
	public void updateBranch() throws SQLException {
		if (!isPrimary()) {
			return;
		}
		if (branch.getOperationId() != null) {
			branch.getRecordInfo().setUpdateDate(new Date());
			branch.getRecordInfo().setUpdateBy(this.login);
			new OperationDao().updateOperation(branch.getOperationId(), branch);
		} else {
			branch.setOperationId(this.branchId);
			branch.getRecordInfo().setCreateDate(new Date());
			branch.getRecordInfo().setCreateBy(this.login);
			new OperationDao().createOperation(this.branchId, branch.getOperationId(), branch);
		}
	}

	@Override
	public IOperation loadBranch() throws SQLException {
		if (branch != null) {
			return branch;
		}
		branch = (Operation) new OperationDao().loadBranch(branchId);
		if (branch == null) {
			branch = new Operation(null, this.branchId);
		}
		return branch;
	}

	@Override
	public IPagingList<IOperation, IOperationSort> getStorePagingList() throws SQLException {
		return storePagingList;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}
}
