package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Operation;

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
	public JsonArray generate(String targetId, String gpsZone, int page) throws SQLException {

		Operation targetStore = (Operation) new OperationDao().loadStore(targetId);
		Operation targetBranch = (Operation) new OperationDao().loadBranch(targetStore.getBranchId());
		DrillResult dr = new DrillResult();

		//5 stores belong in same branch with this store, and 3 similar branches
		JsonArray ja = findStoresOfBranch(targetBranch.getId(), targetId, 0, 8);
		JsonArray ja2 = BranchDriller.findBranchesSimilar(targetBranch.getId(), 8);
		dr.add("branches", ja, 5, ja2, 3);
		
		//Some active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {targetBranch.getId()}, 10);
		dr.add("promos", ja, 5);
		return dr.toJson();
	}

	static JsonArray findStoresOfBranch(String targetBranchId, String excludeStoreId, int offset, int count) throws SQLException { //TODO count

		DbIterator<Operation> stores = new OperationDao().iterateStores(targetBranchId, excludeStoreId, offset, count);
		JsonArray ja = new JsonArray();
		while (stores.hasNext()) {
			Operation one = stores.next();
			ja.add(one.generateFeedJson());
		}
		stores.close();
		return ja;
	}
}