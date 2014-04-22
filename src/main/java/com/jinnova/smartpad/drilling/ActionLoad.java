package com.jinnova.smartpad.drilling;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.sql.SQLException;
import java.util.HashMap;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogItem;
import com.jinnova.smartpad.partner.ICatalogItemSort;
import com.jinnova.smartpad.partner.ICatalogSort;
import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.Operation;

public abstract class ActionLoad {
	
	static final String REL_SIMILAR = "sim";
	
	static final String REL_BELONG_DIRECTLY = "beld";
	
	static final String REL_BELONG_INHERIT = "beli";
	
	static final String REL_SIBLING = "sib";
	
	String branchId;
	
	String storeId;
	
	String catId;
	
	String syscatId;
	
	final String anchorType;

	final String targetType;
	
	final String relation;
	
	String anchorId;
	
	String excludeId;
	
	private int offset;
	
	private int pageSize;
	
	private int initialLoadSize;
	
	private int initialDrillSize;

	private boolean more = true;
	
	private static HashMap<String, Class<? extends ActionLoad>> actionClasses;
	
	public static void initialize() {
		actionClasses = new HashMap<String, Class<? extends ActionLoad>>();
		register(new ALBranchesBelongToSyscat());
		register(new ALCatalogsBelongDirectlyToCatalog());
		register(new ALCatItemBelongDirectlyToCatalog());
		register(new ALPromotionsBelongToSyscat());
		register(new ALStoresBelongToBranch());
	}
	
	private static void register(ActionLoad load) {
		String key = load.anchorType + load.targetType + load.relation;
		if (actionClasses.containsKey(key)) {
			throw new RuntimeException(key);
		}
		actionClasses.put(key, load.getClass());
	}
	
	static Object[] loadMore(String targetType, String anchorType, String anchorId, String relation,
			String branchId, String storeId, String catId, String syscatId, String excludeId,
			String gpsLon, String gpsLat, int offset, int size) throws SQLException {
		
		Class<? extends ActionLoad> c = actionClasses.get(anchorType + targetType + relation);
		try {
			ActionLoad load = c.newInstance();
			load.anchorId = anchorId;
			load.excludeId = excludeId;
			load.offset = offset;
			load.pageSize = size;
			return load.load();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	int getInitialLoadSize() {
		return initialLoadSize;
	}

	int getInitialDrillSize() {
		return initialDrillSize;
	}
	
	void setOffset(int offset) {
		this.offset = offset;
	}
	
	ActionLoad(String anchorType, String targetType, String relation) {
		this.anchorType = anchorType;
		this.targetType = targetType;
		this.relation = relation;
	}
	
	void setParams(String anchorId, String excludeId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this.anchorId = anchorId;
		this.excludeId = excludeId;
		this.pageSize = pageSize;
		this.initialLoadSize = initialLoadSize;
		this.initialDrillSize = initialDrillSize;
	}
	
	ActionLoad exclude(String excludeId) {
		this.excludeId = excludeId;
		return this;
	}

	String generateNextLoadUrl() {
		if (!more) {
			return null;
		}
		StringBuffer buffer = new StringBuffer(targetType + "/" + relation + "/" + anchorType + 
				"/" + anchorId + "?offset=" + offset + "&size="  + pageSize);
		if (branchId != null) {
			buffer.append("&branchId=" + branchId);
		}
		if (storeId != null) {
			buffer.append("&storeId=" + storeId);
		}
		if (catId != null) {
			buffer.append("&catId=" + catId);
		}
		if (syscatId != null) {
			buffer.append("&syscatId=" + syscatId);
		}
		if (excludeId != null) {
			buffer.append("&excludeId=" + excludeId);
		}
		return buffer.toString();
	}
	
	abstract Object[] load(int offset, int size) throws SQLException;
	
	Object[] loadFirstEntries() throws SQLException {
		return loadFirstEntries(initialLoadSize);
	}
	
	abstract Object[] loadFirstEntries(int initialLoadSize) throws SQLException;
	
	final Object[] load() throws SQLException {
		Object[] result = load(offset, pageSize);
		if (result == null || result.length < pageSize) {
			more = false;
		}
		offset += result.length;
		return result;
	}
}

class ALBranchesBelongToSyscat extends ActionLoad {
	
	ALBranchesBelongToSyscat() {
		super(TYPENAME_SYSCAT, TYPENAME_BRANCH, REL_BELONG_DIRECTLY);
	}

	ALBranchesBelongToSyscat(String anchorSyscatId, String excludeBranchId,
			int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		setParams(anchorSyscatId, excludeBranchId, pageSize, initialLoadSize, initialDrillSize);
	}

	@Override
	Object[] load(int offset, int size) throws SQLException {
		return new OperationDao().iterateSimilarBranches(excludeId, anchorId).toArray(); //TODO size, offset
	}
	
	@Override
	Object[] loadFirstEntries(int size) throws SQLException {
		return new OperationDao().iterateSimilarBranches(excludeId, anchorId).toArray(); //TODO size
	}
	
}

class ALStoresBelongToBranch extends ActionLoad {
	
	ALStoresBelongToBranch() {
		super(TYPENAME_BRANCH, TYPENAME_STORE, REL_BELONG_DIRECTLY);
	}

	ALStoresBelongToBranch(String anchorBranchId, String excludeStoreId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		setParams(anchorBranchId, excludeStoreId, pageSize, initialLoadSize, initialDrillSize);
	}

	@Override
	Object[] load(int offset, int size) throws SQLException {
		return new OperationDao().iterateStores(anchorId, excludeId, offset, size).toArray();
	}
	
	@Override
	Object[] loadFirstEntries(int size) throws SQLException {
		return new OperationDao().iterateStores(anchorId, excludeId, 0, size).toArray();
	}
	
}

class ALCatalogsBelongDirectlyToCatalog extends ActionLoad {
	
	ALCatalogsBelongDirectlyToCatalog() {
		super(TYPENAME_CAT, TYPENAME_CAT, REL_BELONG_DIRECTLY);
	}

	ALCatalogsBelongDirectlyToCatalog(String catId, String excludeCatId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		setParams(catId, excludeCatId, pageSize, initialLoadSize, initialDrillSize);
	}

	@Override
	Object[] load(int offset, int size) throws SQLException {
		String syscatId = null; //don't need to parse spec 
		CachedPagingList<ICatalog, ICatalogSort> paging = Catalog.createSubCatalogPagingList(null, null, anchorId, syscatId, null);
		paging.setPageSize(size);
		return paging.loadFromOffset(offset).getPageEntries();
	}
	
	@Override
	Object[] loadFirstEntries(int size) throws SQLException {
		String syscatId = null; //don't need to parse spec 
		CachedPagingList<ICatalog, ICatalogSort> paging = Catalog.createSubCatalogPagingList(null, null, anchorId, syscatId, null);
		paging.setPageSize(size);
		return paging.loadFromOffset(0).getPageEntries();
	}
	
}

class ALCatItemBelongDirectlyToCatalog extends ActionLoad {
	
	ALCatItemBelongDirectlyToCatalog() {
		super(TYPENAME_CAT, TYPENAME_CATITEM, REL_BELONG_DIRECTLY);
	}

	ALCatItemBelongDirectlyToCatalog(String catId, String syscatId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		setParams(catId, null, pageSize, initialLoadSize, initialDrillSize);
		this.syscatId = syscatId;
	}

	@Override
	Object[] load(int offset, int size) throws SQLException {
		CachedPagingList<ICatalogItem, ICatalogItemSort> paging = Catalog.createCatalogItemPagingList(null, null, anchorId, syscatId, null);
		paging.setPageSize(size);
		return paging.loadFromOffset(offset).getPageEntries();
	}
	
	@Override
	Object[] loadFirstEntries(int size) throws SQLException {
		CachedPagingList<ICatalogItem, ICatalogItemSort> paging = Catalog.createCatalogItemPagingList(null, null, anchorId, syscatId, null);
		paging.setPageSize(size);
		return paging.loadFromOffset(0).getPageEntries();
	}
	
}

class ALPromotionsBelongToSyscat extends ActionLoad {
	
	ALPromotionsBelongToSyscat() {
		super(TYPENAME_BRANCH, TYPENAME_PROMO, REL_BELONG_INHERIT);
	}

	ALPromotionsBelongToSyscat(String syscatId, String preferedBranchId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		setParams(syscatId, null, pageSize, initialLoadSize, initialDrillSize);
	}

	@Override
	Object[] load(int offset, int size) throws SQLException {
		CachedPagingList<IPromotion, IPromotionSort> paging = 
				Operation.createPromotionPagingList(anchorId, null, /*gps*/null); //TODO gps
		paging.setPageSize(size);
		return paging.loadFromOffset(offset).getPageEntries();
	}
	
	@Override
	Object[] loadFirstEntries(int size) throws SQLException {
		CachedPagingList<IPromotion, IPromotionSort> paging = 
				Operation.createPromotionPagingList(anchorId, null, /*gps*/null); //TODO gps
		paging.setPageSize(size);
		return paging.loadFromOffset(0).getPageEntries();
	}
	
}
