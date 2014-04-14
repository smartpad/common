package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
	public String generate(String targetId, String gpsZone, int page) throws SQLException {

		//All stores belong in same branch with this store
		JsonObject storeJson = new JsonObject();
		Operation targetStore = (Operation) new OperationDao().loadStore(targetId);
		Operation targetBranch = (Operation) new OperationDao().loadBranch(targetStore.getBranchId());
		JsonArray ja = findStores(targetBranch.getId(), targetId);
		storeJson.add("stores", ja);
		
		//Some similar branches in one compound
		ja = BranchDriller.findBranches(targetBranch.getId());
		storeJson.add("branches", ja);
		
		//Some active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {targetBranch.getId()});
		storeJson.add("promos", ja);
		return storeJson.toString();
	}

	static JsonArray findStores(String targetBranchId, String targetStoreId) throws SQLException {

		DbIterator<Operation> stores = new OperationDao().iterateStores(targetBranchId);
		JsonArray ja = new JsonArray();
		while (stores.hasNext()) {
			Operation one = stores.next();
			if (targetStoreId != null && one.getId().equals(targetStoreId)) {
				continue;
			}
			ja.add(one.generateFeedJson());
		}
		stores.close();
		return ja;
	}
}
