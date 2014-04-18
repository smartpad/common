package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Operation;

import static com.jinnova.smartpad.partner.IDetailManager.*;
import static com.jinnova.smartpad.drilling.ActionLoad.*;

class StoreDriller implements DetailDriller {
	
	/**
     * Returns in order the following:
     * 
     *  - Extra details of this store
     *  - Other stores in same branch & similar branches
     * 	- Some active promotions from this store in one compound
     * 	- All sub categories of this store's root category in one compound
     * 	- 2 posts from this store
     * 	- Feature catelog items from this store's root category
     *
     */
	@Override
	public void drill(String targetId, String gpsZone, int page, int size, JsonObject resultJson) throws SQLException {

		OperationDao odao = new OperationDao();
		Operation targetStore = odao.loadStore(targetId);
		Operation targetBranch = odao.loadBranch(targetStore.getBranchId());
		DrillResult dr = new DrillResult();

		//5 stores belong in same branch with this store, and 3 similar branches
		Object[] ja = new OperationDao().iterateStores(targetBranch.getId(), targetId, 0, 8).toArray();
		Object[] ja2 = odao.iterateSimilarBranches(targetBranch.getId(), targetBranch.getSyscatId()).toArray();
		dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
				new DrillSectionSimple(TYPENAME_STORE, ja, 5, 
						new ActionLoad(TYPENAME_BRANCH, targetBranch.getId(), TYPENAME_STORE, REL_BELONG, 10).exclude(targetId)),
				new DrillSectionSimple(TYPENAME_BRANCH, ja2, 3, 
						new ActionLoad(TYPENAME_BRANCH, targetBranch.getId(), TYPENAME_BRANCH, REL_SIMILAR, 10)));
		
		//Some active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {targetBranch.getId()}, 10);
		dr.add(TYPENAME_COMPOUND_PROMOS, ja, 5, new ActionLoad(TYPENAME_BRANCH, targetBranch.getId(), TYPENAME_PROMO, REL_BELONG, 10));
		dr.writeJson(resultJson);
	}

	/*static Object[] findStoresOfBranch(String targetBranchId, String excludeStoreId, int offset, int count) throws SQLException {

		CachedPagingList<IOperation, IOperationSort> paging = User.createStorePagingList(targetBranchId, branchSyscatIdnull, branchGpsnull);
		paging.setPageSize(count);
		return paging.loadPage(1).getPageEntries();
		
		DbIterator<Operation> stores = new OperationDao().iterateStores(targetBranchId, excludeStoreId, offset, count);
		LinkedList<Object> ja = new LinkedList<>();
		while (stores.hasNext()) {
			ja.add(stores.next());
		}
		stores.close();
		return ja.toArray();
	}*/
}
