package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.PageEntrySupport;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.db.CatalogItemDao;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.db.PromotionDao;
import com.jinnova.smartpad.partner.IUser;

public class User implements IUser {

	private String login;
	
	private String loginTemp;
	
	private String passhash;

	public final String branchId;
	
	private Operation branch;
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private CachedPagingList<IOperation, IOperationSort> storePagingList;

	public User(String login, String branchId, String passhash) {
		super();
		this.login = login;
		this.branchId = branchId;
		this.passhash = passhash;
		//this.storePagingList = createStorePagingList(branchId, branchSyscatId, branchGps);
	}
	
	public static CachedPagingList<IOperation, IOperationSort> createStorePagingList(
			final String branchId, final String branchSyscatId, final GPSInfo branchGps) {
		
		@SuppressWarnings("unchecked")
		final Comparator<IOperation>[] storeComparators = new Comparator[IOperationSort.values().length];
		storeComparators[IOperationSort.creation.ordinal()] = new Comparator<IOperation>() {
			@Override
			public int compare(IOperation o1, IOperation o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o2.getRecordInfo().getCreateDate());
			}
		};
		storeComparators[IOperationSort.lastUpdate.ordinal()] = new Comparator<IOperation>() {
			@Override
			public int compare(IOperation o1, IOperation o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		storeComparators[IOperationSort.name.ordinal()] = new Comparator<IOperation>() {
			@Override
			public int compare(IOperation o1, IOperation o2) {
				return o1.getName().getName().compareToIgnoreCase(o2.getName().getName());
			}
		};
		PageEntrySupport<IOperation, IOperationSort> storeMate = new PageEntrySupport<IOperation, IOperationSort>() {

			@Override
			public IOperation newEntryInstance(IUser authorizedUser) {
				Operation op = new Operation(null, branchId, branchSyscatId,
						branchGps.getLongitude(), branchGps.getLatitude(), GPSInfo.INHERIT_BRANCH, false);
				//op.gps.inherit(branch.gps, GPSInfo.INHERIT_BRANCH);
				return op;
			}

			@Override
			public boolean isPersisted(IOperation member) {
				return ((Operation) member).getId() != null;
			}

			@Override
			public LinkedList<IOperation> load(IUser authorizedUser, 
					int offset, int pageSize, IOperationSort sortField, boolean ascending) throws SQLException {
				
				return new OperationDao().loadStores(branchId, offset, pageSize, sortField, ascending);
			}

			@Override
			public void insert(IUser authorizedUser, IOperation t) throws SQLException {
				Operation op = (Operation) t;
				if (((Catalog) op.getRootCatalog()).getSystemCatalogId() == null) {
					throw new RuntimeException("A system catalog must be assigned to a store");
				}
				String newId = SmartpadCommon.md5(branchId +  op.getName());
				op.setId(newId);
				new OperationDao().createOperation(newId, branchId, op);
			}

			@Override
			public void update(IUser authorizedUser, IOperation t) throws SQLException {
				Operation op = (Operation) t;
				if (((Catalog) op.getRootCatalog()).getSystemCatalogId() == null) {
					throw new RuntimeException("A system catalog must be assigned to a store");
				}
				boolean gpsModified = op.gps.isModified();
				new OperationDao().updateOperation(op.getId(), op);
				if (gpsModified) {
					new CatalogDao().updateStoreGps(op.getId(), op.gps.getLongitude(), op.gps.getLatitude());
					new CatalogItemDao().updateStoreGps(op, op.gps.getLongitude(), op.gps.getLatitude());
					new PromotionDao().updateStoreGps(op.getId(), op.gps.getLongitude(), branchGps.getLatitude());
				}
			}

			@Override
			public void delete(IUser authorizedUser, IOperation t) throws SQLException {
				Operation op = (Operation) t;
				new OperationDao().deleteOperation(op.getId());
				op.setId(null);
			}

			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new OperationDao().countStores(branchId);
			}
		};
		return new CachedPagingList<IOperation, IOperationSort>(storeMate, storeComparators, IOperationSort.creation, new IOperation[0]);
	}
	
	@Override
	public boolean isPrimary() {
		return this.login.equals(branch.getBranchId());
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
	
	/*String getBranchId() {
		return this.branchId;
	}*/

	@Override
	public void setPassword(String password) {
		this.passhash = SmartpadCommon.md5(password);
	}

	@Override
	public void updateBranch() throws SQLException {
		
		if (!isPrimary()) {
			return;
		}
		if (((Catalog) branch.getRootCatalog()).getSystemCatalogId() == null) {
			throw new RuntimeException("A system catalog must be assigned to a branch");
		}
		
		if (branch.getId() != null) {
			boolean gpsModified = branch.gps.isModified();
			branch.getRecordInfo().setUpdateDate(new Date());
			branch.getRecordInfo().setUpdateBy(this.login);
			new OperationDao().updateOperation(branch.getId(), branch);
			if (gpsModified) {
				new OperationDao().updateBranchGps(branch.getId(), branch.gps.getLongitude(), branch.gps.getLatitude());
				new CatalogDao().updateBranchGps(branch.getId(), branch.gps.getLongitude(), branch.gps.getLatitude());
				new CatalogItemDao().updateBranchGps(this, branch.gps.getLongitude(), branch.gps.getLatitude());
				new PromotionDao().updateBranchGps(branch.getId(), branch.gps.getLongitude(), branch.gps.getLatitude());
			}
		} else {
			branch.setId(this.branch.getBranchId());
			branch.getRecordInfo().setCreateDate(new Date());
			branch.getRecordInfo().setCreateBy(this.login);
			new OperationDao().createOperation(this.branch.getBranchId(), branch.getId(), branch);
		}
	}
	
	void loadBranch(/*String branchId*/) throws SQLException {
		branch = (Operation) new OperationDao().loadBranch(branchId);
		if (branch == null) {
			branch = new Operation(branchId, branchId, null, null, null, null, true);
		}
		//((Catalog) branch.getRootCatalog()).gps.inherit(branch.gps, GPSInfo.INHERIT_BRANCH);
		//this.branchId = branch.getId();
		
		String syscatId = ((Catalog) branch.getRootCatalog()).getSystemCatalogId();
		if (syscatId != null) {
			this.storePagingList = createStorePagingList(branchId, syscatId, branch.gps);
		}
	}
	
	void setBranch(Operation branch) {
		this.branch = branch;
		//branchId = branch.getId();
	}
	
	@Override
	public IOperation getBranch() {
		return this.branch;
	}
	
	/*String getBranchId() {
		return this.branch.getBranchId();
	}*/

	@Override
	public IPagingList<IOperation, IOperationSort> getStorePagingList() throws SQLException {
		return storePagingList;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}
}
