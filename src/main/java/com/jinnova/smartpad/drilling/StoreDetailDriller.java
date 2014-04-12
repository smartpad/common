package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.db.PromotionDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.Promotion;

public class StoreDetailDriller implements DetailDriller {
	
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
		DbIterator<Operation> stores = new OperationDao().iterateStores(targetBranch.getId());
		JsonArray testJa = new JsonArray();
		JsonArray ja = new JsonArray();
		while (stores.hasNext()) {
			Operation one = stores.next();
			if (one.getId().equals(targetId)) {
				continue;
			}
			ja.add(one.generateFeedJson());
			testJa.add(one.generateFeedJson());
		}
		stores.close();
		storeJson.add("stores", ja);
		
		//Some similar branches in one compound
		String syscatId = ((Catalog) targetBranch.getRootCatalog()).getSystemCatalogId();
		DbIterator<Operation> similarBranches = new OperationDao().iterateSimilarBranches(targetId, syscatId);
		ja = new JsonArray();
		while (similarBranches.hasNext()) {
			Operation one = similarBranches.next();
			if (one.getId().equals(targetBranch.getId())) {
				continue;
			}
			ja.add(one.generateFeedJson());
			testJa.add(one.generateFeedJson());
		}
		similarBranches.close();
		storeJson.add("branches", ja);
		
		//Some active promotions from this branch in one compound
		DbIterator<Promotion> promos = new PromotionDao().iteratePromos(targetBranch.getId());
		ja = new JsonArray();
		while (promos.hasNext()) {
			Promotion one = promos.next();
			ja.add(one.generateFeedJson());
			testJa.add(one.generateFeedJson());
		}
		promos.close();
		storeJson.add("promos", ja);
		
		//return storeJson.toString();
		
		JsonObject o = new JsonObject();
		o.addProperty("v", "A");
		o.add("t", testJa);
		return o.toString();
	}

}
