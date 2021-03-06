package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.SYSTEM_BRANCH_ID;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.ImageSupport;
import com.jinnova.smartpad.PageEntrySupport;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.CatalogItemDao;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.db.ScriptRunner;
import com.jinnova.smartpad.db.UserDao;

public class PartnerManager implements IPartnerManager {
	
	public static PartnerManager instance;
	
	public final User systemUser;
	
	private final CachedPagingList<IUser, IUserSort> userPagingList;
	
	private final Catalog systemRootCatalog;
	
	private final HashMap<String, Catalog> systemCatMap = new HashMap<>();
	private final HashMap<String, LinkedList<Catalog>> systemSubCatMap = new HashMap<>();
	
	//private final HashMap<String, ICatalogSpec> catalogSpecMap = new HashMap<>();
	
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
		systemRootCatalog.setBranchName(SYSTEM_BRANCH_ID);
		systemRootCatalog.setName("");
	}
	
	public static void initialize(String dbhost, String dbport, String dbname, String dblogin, String dbpass, 
			String imageInQueuePath, String imageOutRoot) throws SQLException {
		
		SmartpadConnectionPool.initialize(dblogin, dbpass,  ScriptRunner.makeDburl(dbhost, dbport, dbname));
		ImageSupport.initialize(imageInQueuePath, imageOutRoot);
		instance = new PartnerManager();
		loadSyscatsInitially();
	}
	
	public static void loadSyscatsInitially() throws SQLException {
		instance.systemRootCatalog.loadAllSubCatalogsRecursively(instance.systemCatMap);
		instance.systemCatMap.remove(SYSTEM_BRANCH_ID);
		for (Catalog cat : instance.systemCatMap.values()) {
			LinkedList<Catalog> subCats = instance.systemSubCatMap.get(cat.getParentCatalogId());
			if (subCats == null) {
				subCats = new LinkedList<>();
				instance.systemSubCatMap.put(cat.getParentCatalogId(), subCats);
			}
			subCats.add(cat);
		}
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
		
		Operation branch = new Operation(login, login, IDetailManager.SYSTEM_CAT_ALL, null, null, GPSInfo.INHERIT_PROVIDED, true /*branch*/);
		branch.setName(login);
		branch.getRecordInfo().setCreateBy(login);
		branch.getRecordInfo().setCreateDate(new Date());
		new OperationDao().insertOperation(login, login, branch);
		branch.setId(login);
		u.setBranch(branch);
		//getUserPagingList().put(u, u);
		return u;
	}
	
	public Operation createUnmanagedBranch(String unmanagedBranchId, String systemCatalogId, String branchName) throws SQLException {
		Operation unmanagedBranch = new Operation(unmanagedBranchId, unmanagedBranchId, systemCatalogId, null, null, GPSInfo.INHERIT_PROVIDED, true);
		unmanagedBranch.getRecordInfo().setCreateBy(PartnerManager.instance.systemUser.getLogin());
		unmanagedBranch.getRecordInfo().setCreateDate(new Date());
		unmanagedBranch.setName(branchName);
		unmanagedBranch.setBranchType(IOperation.BRANCH_TYPE_UNMANAGED);
		new OperationDao().insertOperation(unmanagedBranchId, unmanagedBranchId, unmanagedBranch);
		unmanagedBranch.setId(unmanagedBranchId);
		return unmanagedBranch;
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
	public IPagingList<ICatalogItem, ICatalogItemSort> createSyscatItemPagingList(String syscatId) {
		return new CachedPagingList<ICatalogItem, ICatalogItemSort>(new SyscatItemPageEntrySupport(syscatId), 
				Catalog.createCatalogItemComparators(), ICatalogItemSort.createDate, new ICatalogItem[0]);
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
	public ICatalogSpec getCatalogSpec(String systemCatId) {
		Catalog cat = systemCatMap.get(systemCatId);
		if (cat == null) {
			throw new RuntimeException("Invalid syscat id: " + systemCatId);
		}
		return cat.getCatalogSpec();
	}
	
	public LinkedList<Catalog> getSystemSubCatalog(String parentCatId) {
		return systemSubCatMap.get(parentCatId);
	}

	/*@Override
	public void setImage(String typeName, String subTypeName, String entityId, String imageId, InputStream image) throws IOException {
		new ImageSupport().queueIn(typeName, subTypeName, entityId, imageId, image);
	}*/

	@Override
	public BufferedImage getImage(String typeName, String subTypeName,
			String entityId, String imageId, int size) throws IOException {
		
		return new ImageSupport().getImage(typeName, subTypeName, entityId, imageId, size);
	}
	
	/*public DbIterator<User> iterateAllPrimaryUsers(Connection conn) throws SQLException {
		return new UserDao().iterateAllPrimaryUsers(conn);
	}*/
}

class SyscatItemPageEntrySupport implements PageEntrySupport<ICatalogItem, ICatalogItemSort> {
	
	private String syscatId;
	
	private String specId;

	public SyscatItemPageEntrySupport(String syscatId) {
		super();
		this.syscatId = syscatId;
		this.specId = PartnerManager.instance.getCatalogSpec(syscatId).getSpecId();
	}

	@Override
	public ICatalogItem newEntryInstance(IUser authorizedUser) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPersisted(ICatalogItem member) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int count(IUser authorizedUser) throws SQLException {
		return new CatalogItemDao().countCatalogItems(syscatId, specId, true);
	}
	
	@Override
	public LinkedList<ICatalogItem> load(IUser authorizedUser, int offset,
			int pageSize, ICatalogItemSort sortField, boolean ascending) throws SQLException {

		ICatalog syscat = PartnerManager.instance.getSystemCatalog(syscatId);
		return new CatalogItemDao().loadCatalogItems(
				syscatId, syscat.getCatalogSpec(), true, offset, pageSize, sortField, ascending);
	}

	@Override
	public void insert(IUser authorizedUser, ICatalogItem newMember) throws SQLException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void update(IUser authorizedUser, ICatalogItem member) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(IUser authorizedUser, ICatalogItem member) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
}