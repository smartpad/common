package com.jinnova.smartpad.partner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.PageEntrySupport;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.db.ScriptRunner;
import com.jinnova.smartpad.db.UserDao;
import com.jinnova.smartpad.drilling.ActionLoad;
import com.jinnova.smartpad.drilling.DetailManager;

public class PartnerManager implements IPartnerManager {
	
	private static final String SYSTEM_BRANCH_ID = "z";
	
	public static PartnerManager instance;
	
	public final User systemUser;
	
	private final CachedPagingList<IUser, IUserSort> userPagingList;
	
	private final Catalog systemRootCatalog;
	
	private final HashMap<String, Catalog> systemCatMap = new HashMap<>();
	
	private final HashMap<String, ICatalogSpec> catalogSpecMap = new HashMap<>();
	
	private PartnerManager() throws SQLException {
		
		systemUser = new User(SYSTEM_BRANCH_ID, SYSTEM_BRANCH_ID, null);
		//systemUser.loadBranch("SMARTPAD");
		systemUser.loadBranch();
		
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
		PageEntrySupport<IUser, IUserSort> memberMate = new PageEntrySupport<IUser, IUserSort>() {

			@Override
			public IUser newEntryInstance(IUser authorizedUser) {
				User u = new User(null, ((User) authorizedUser).branchId, null);
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
				
				return new UserDao().listUsers(((User) authorizedUser).branchId, offset, pageSize, sortField, ascending);
			}

			@Override
			public void insert(IUser authorizedUser, IUser newMember) throws SQLException {
				User newUser = (User) newMember;
				newUser.setLogin();
				new UserDao().createUser(newUser.branchId, newUser);
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
				return new UserDao().count(((User) authorizedUser).branchId);
			}
		};
		
		userPagingList = new CachedPagingList<IUser, IUserSort>(memberMate, comparators, IUserSort.creation, new IUser[0]);
		systemRootCatalog = new Catalog(SYSTEM_BRANCH_ID, SYSTEM_BRANCH_ID, SYSTEM_BRANCH_ID, null, null);
	}
	
	public static void initialize() throws SQLException {
		SmartpadConnectionPool.initialize("root", "", "jdbc:mysql://localhost/smartpad?useUnicode=true&characterEncoding=UTF-8");
		instance = new PartnerManager();
		instance.systemRootCatalog.loadAllSubCatalogsRecursively(instance.systemCatMap);
		instance.systemCatMap.remove(SYSTEM_BRANCH_ID);
		DetailManager.initialize();
		ActionLoad.initialize();
	}

	public void clearDatabaseForTests() throws SQLException, FileNotFoundException, IOException {
		ScriptRunner.clearDatabaseForTests();
		new SystemCatalogGenrator().generate();
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
		User u = new User(login, login, SmartpadCommon.md5(password));
		((RecordInfo) u.getRecordInfo()).setCreateBy(login);
		((RecordInfo) u.getRecordInfo()).setCreateDate(new Date());
		new UserDao().createUser(login, u);
		
		Operation branch = new Operation(null, login, null, 0, 0, GPSInfo.INHERIT_BRANCH, true);
		branch.getName().setName("");
		branch.getRootCatalog().setSystemCatalogId("foods"); //TODO default place for new branch?
		branch.getRecordInfo().setCreateBy(login);
		branch.getRecordInfo().setCreateDate(new Date());
		new OperationDao().createOperation(login, login, branch);
		branch.setId(login);
		u.setBranch(branch);
		return u;
	}

	@Override
	public IUser login(String login, String password) throws SQLException {
		if (password == null || login == null) {
			return null;
		}
		//String[] branchId = new String[1];
		User u = (User) new UserDao().loadUser(login/*, branchId*/);
		if (u == null) {
			return null;
		}
		if (!SmartpadCommon.md5(password).equals(u.getPasshash())) {
			return null;
		}
		u.loadBranch(/*branchId[0]*/);
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

	void putSystemCatalog(Catalog cat) {
		systemCatMap.put(cat.getId(), cat);
	}

	@Override
	public ICatalog getSystemCatalog(String systemCatId) {
		return systemCatMap.get(systemCatId);
	}
	
	@Override
	public ICatalogSpec getCatalogSpec(String specId) {
		return catalogSpecMap.get(specId);
	}
	
	/*public DbIterator<User> iterateAllPrimaryUsers(Connection conn) throws SQLException {
		return new UserDao().iterateAllPrimaryUsers(conn);
	}*/
}
