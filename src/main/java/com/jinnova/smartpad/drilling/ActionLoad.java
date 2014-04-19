package com.jinnova.smartpad.drilling;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.sql.SQLException;
import java.util.HashMap;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.Operation;

class ActionLoad {
	
	static final String REL_SIMILAR = "sim";
	
	static final String REL_BELONG = "bel";
	
	static final String REL_SIBLING = "sib";
	
	String anchorType;
	
	String anchorId;

	String targetType;
	
	String relation;
	
	int offset;
	
	int pageSize;
	
	int initialLoadSize;
	
	int initialDrillSize;
	
	String excludeId;
	
	private boolean more = true;
	
	private static HashMap<String, Class<? extends ActionLoad>> actionClasses;
	
	public static void initialize() {
		actionClasses = new HashMap<String, Class<? extends ActionLoad>>();
		register(new ALBranchesBelongToSyscat());
		register(new ALPromotionsBelongToBranch());
		register(new ALStoresBelongToBranch());
	}
	
	private static void register(ActionLoad load) {
		String key = load.anchorType + load.targetType + load.relation;
		if (actionClasses.containsKey(key)) {
			throw new RuntimeException(key);
		}
		actionClasses.put(key, load.getClass());
	}
	
	public static Object[] loadMore(String anchorType, String targetType, String relation,
			String anchorId, String excludeId, int offset, int pageSize) throws SQLException {
		
		Class<? extends ActionLoad> c = actionClasses.get(anchorType + targetType + relation);
		try {
			ActionLoad load = c.newInstance();
			load.anchorId = anchorId;
			load.excludeId = excludeId;
			load.offset = offset;
			load.pageSize = pageSize;
			return load.execute();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	ActionLoad(String anchorType, String anchorId, String targetType, String rel, int pageSize) {
		
	}

	/*ActionLoad(String[] identifiers, String anchorId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this(identifiers);
		this.anchorId = anchorId;
		this.pageSize = pageSize;
		this.initialLoadSize = initialLoadSize;
		this.initialDrillSize = initialDrillSize;
	}*/
	
	ActionLoad(String[] identifiers) {
		this.anchorType = identifiers[0];
		this.targetType = identifiers[1];
		this.relation = identifiers[2];
	}
	
	ActionLoad exclude(String excludeId) {
		this.excludeId = excludeId;
		return this;
	}

	String generateNextLoadUrl() {
		if (!more) {
			return null;
		}
		return targetType + "/" + relation + "?anchorType=" + anchorType + 
				"&anchorId=" + anchorId + "&offset=" + offset + "&size="  + pageSize;
	}
	
	Object[] executeInternal() throws SQLException {
		return null;
	}
	
	Object[] executeInitial() throws SQLException {
		return null;
	}
	
	final Object[] execute() throws SQLException {
		Object[] result = executeInternal();
		if (result == null || result.length < pageSize) {
			more = false;
		}
		offset += result.length;
		return result;
	}
}

class ALStoresBelongToBranch extends ActionLoad {
	
	ALStoresBelongToBranch() {
		super(new String[] {TYPENAME_BRANCH, TYPENAME_STORE, REL_BELONG});
	}

	ALStoresBelongToBranch(String anchorBranchId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		this.anchorId = anchorBranchId;
		this.pageSize = pageSize;
		this.initialLoadSize = initialLoadSize;
		this.initialDrillSize = initialDrillSize;
	}

	@Override
	Object[] executeInternal() throws SQLException {
		return new OperationDao().iterateStores(anchorId, excludeId, offset, pageSize).toArray();
	}
	
	Object[] executeInitial() throws SQLException {
		return new OperationDao().iterateStores(anchorId, excludeId, 0, initialLoadSize).toArray();
	}
	
}

class ALBranchesBelongToSyscat extends ActionLoad {
	
	ALBranchesBelongToSyscat() {
		super(new String[] {TYPENAME_SYSCAT, TYPENAME_BRANCH, REL_BELONG});
	}

	ALBranchesBelongToSyscat(String anchorSyscatId, String excludeBranchId,
			int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		this.anchorId = anchorSyscatId;
		this.pageSize = pageSize;
		this.initialLoadSize = initialLoadSize;
		this.initialDrillSize = initialDrillSize;
	}

	@Override
	Object[] executeInternal() throws SQLException {
		return new OperationDao().iterateSimilarBranches(excludeId, anchorId).toArray(); //TODO size, offset
	}
	
	Object[] executeInitial() throws SQLException {
		return new OperationDao().iterateSimilarBranches(excludeId, anchorId).toArray(); //TODO size
	}
	
}

class ALPromotionsBelongToBranch extends ActionLoad {
	
	ALPromotionsBelongToBranch() {
		super(new String[] {TYPENAME_BRANCH, TYPENAME_PROMO, REL_BELONG});
	}

	ALPromotionsBelongToBranch(String branchId, int pageSize, int initialLoadSize, int initialDrillSize) {
		this();
		this.anchorId = branchId;
		this.pageSize = pageSize;
		this.initialLoadSize = initialLoadSize;
		this.initialDrillSize = initialDrillSize;
	}

	@Override
	Object[] executeInternal() throws SQLException {
		CachedPagingList<IPromotion, IPromotionSort> paging = 
				Operation.createPromotionPagingList(anchorId, null, /*gps*/null); //TODO gps
		paging.setPageSize(pageSize);
		//TODO offset
		return paging.loadPage(1).getPageEntries();
	}
	
	Object[] executeInitial() throws SQLException {
		CachedPagingList<IPromotion, IPromotionSort> paging = 
				Operation.createPromotionPagingList(anchorId, null, /*gps*/null); //TODO gps
		paging.setPageSize(initialLoadSize);
		return paging.loadPage(1).getPageEntries();
	}
	
}
