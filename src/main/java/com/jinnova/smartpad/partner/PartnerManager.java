package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.PageMemberMate;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.UserDao;

public class PartnerManager implements IPartnerManager {
	
	public static PartnerManager instance;
	
	final User systemUser;
	
	private final CachedPagingList<IUser, IUserSort> userPagingList;
	
	private final Catalog systemRootCatalog;
	
	private final HashMap<String, Catalog> systemCatMap = new HashMap<>();
	
	private final HashMap<String, ICatalogSpec> catalogSpecMap = new HashMap<>();
	
	private PartnerManager() throws SQLException {
		
		systemUser = new User("SMARTPAD", /*"SMARTPAD",*/ null);
		systemUser.loadBranch("SMARTPAD");
		
		@SuppressWarnings("unchecked")
		final Comparator<IUser>[] comparators = new Comparator[3];
		comparators[IUserSort.creation.ordinal()] = new Comparator<IUser>() {
			
			@Override
			public int compare(IUser o1, IUser o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o2.getRecordInfo().getCreateDate());
			}
		};
		comparators[IUserSort.lastUpdate.ordinal()] = new Comparator<IUser>() {
			
			@Override
			public int compare(IUser o1, IUser o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		comparators[IUserSort.login.ordinal()] = new Comparator<IUser>() {
			
			@Override
			public int compare(IUser o1, IUser o2) {
				return o1.getLogin().compareTo(o2.getLogin());
			}
		};
		PageMemberMate<IUser, IUserSort> memberMate = new PageMemberMate<IUser, IUserSort>() {

			@Override
			public IUser newMemberInstance(IUser authorizedUser) {
				User u = new User(null, /*((User) authorizedUser).getBranch().getBranchId(),*/ null);
				u.setBranch((Operation) ((User) authorizedUser).getBranch());
				return u;
			}

			@Override
			public boolean isPersisted(IUser member) {
				return ((User) member).getLogin() != null;
			}

			@Override
			public LinkedList<IUser> load(IUser authorizedUser, int offset, int pageSize,
					IUserSort sortField, boolean ascending) throws SQLException {
				
				return new UserDao().listUsers(((User) authorizedUser).getBranchId(), offset, pageSize, sortField, ascending);
			}

			@Override
			public void insert(IUser authorizedUser, IUser newMember) throws SQLException {
				User newUser = (User) newMember;
				newUser.setLogin();
				new UserDao().createUser(newUser.getBranchId(), newUser);
			}

			@Override
			public void update(IUser authorizedUser, IUser member) throws SQLException {
				if (!authorizedUser.isPrimary() && !authorizedUser.getLogin().equals(member.getLogin())) {
					return;
				}
				new UserDao().updateUser(member);
			}

			@Override
			public void delete(IUser authorizedUser, IUser member) throws SQLException {
				new UserDao().deleteUser(member);
			}

			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new UserDao().count(((User) authorizedUser).getBranchId());
			}
		};
		
		userPagingList = new CachedPagingList<IUser, IUserSort>(memberMate, comparators, IUserSort.creation, new IUser[0]);
		systemRootCatalog = new Catalog("SMARTPAD", "SMARTPAD", null, null);
	}
	
	public static void initialize() throws SQLException {
		SmartpadConnectionPool.initialize("root", "", "jdbc:mysql://localhost/smartpad");
		instance = new PartnerManager();
		instance.systemRootCatalog.loadAllSubCatalogsRecursively(instance.systemCatMap);
	}

	public void clearDatabaseForTests() throws SQLException {
		new UserDao().clearDatabaseForTests();
	}
	
	/*public Iterator<CatalogItem> storeItemIterator() {
		return null;
	}
	
	public Iterator<Promotion> promotionIterator() {
		return null;
	}*/
	
	@Override
	public IUser getSystemUser() {
		return this.systemUser;
	}
	
	@Override
	public IUser createPrimaryUser(String login, String password) throws SQLException {
		User u = new User(login, /*login,*/ SmartpadCommon.md5(password));
		((RecordInfo) u.getRecordInfo()).setCreateBy(login);
		((RecordInfo) u.getRecordInfo()).setCreateDate(new Date());
		new UserDao().createUser(login, u);
		
		u.setBranch(new Operation(null, login, null));
		//new OperationDao().createOperation(login);
		return u;
	}

	@Override
	public IUser login(String login, String password) throws SQLException {
		if (password == null || login == null) {
			return null;
		}
		String[] branchId = new String[1];
		User u = (User) new UserDao().loadUser(login, branchId);
		if (u == null) {
			return null;
		}
		if (!SmartpadCommon.md5(password).equals(u.getPasshash())) {
			return null;
		}
		u.loadBranch(branchId[0]);
		return u;
	}

	@Override
	public IPagingList<IUser, IUserSort> getUserPagingList() throws SQLException {
		return userPagingList;
	}

	@Override
	public Catalog getSystemRootCatalog() {
		return this.systemRootCatalog;
	}

	@Override
	public ICatalog getSystemCatalog(String systemCatId) {
		return systemCatMap.get(systemCatId);
	}
	
	@Override
	public ICatalogSpec getCatalogSpec(String specId) {
		return catalogSpecMap.get(specId);
	}
}
