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

class BranchDetailDriller implements DetailDriller {

	/**
     * Returns in order the following:
     * 
     *  - Extra details of this branch
     * 	- All stores belong to this branch in one compound
     * 	- Some similar branches in one compound
     * 	- Some active promotions from this branch in one compound
     * 	- All sub categories of this branch's root category in one compound
     * 	- 2 posts from this branch 
     * 	- Feature catelog items from this branch's root category
     *
     */
	@Override
	public String generate(String targetId, String gpsZone, int page) throws SQLException {
		
		//All stores belong to this branch in one compound
		JsonObject branchJson = new JsonObject();
		DbIterator<Operation> stores = new OperationDao().iterateStores(targetId);
		JsonArray ja = new JsonArray();
		while (stores.hasNext()) {
			Operation one = stores.next();
			ja.add(one.generateFeedJson());
		}
		stores.close();
		branchJson.add("stores", ja);
		
		//Some similar branches in one compound
		Operation targetBranch = (Operation) new OperationDao().loadBranch(targetId);
		String syscatId = ((Catalog) targetBranch.getRootCatalog()).getSystemCatalogId();
		DbIterator<Operation> similarBranches = new OperationDao().iterateSimilarBranches(targetId, syscatId);
		ja = new JsonArray();
		while (similarBranches.hasNext()) {
			Operation one = similarBranches.next();
			ja.add(one.generateFeedJson());
		}
		similarBranches.close();
		branchJson.add("branches", ja);
		
		//Some active promotions from this branch in one compound
		DbIterator<Promotion> promos = new PromotionDao().iteratePromos(targetId);
		ja = new JsonArray();
		while (promos.hasNext()) {
			Promotion one = promos.next();
			ja.add(one.generateFeedJson());
		}
		promos.close();
		branchJson.add("promos", ja);
		
		return branchJson.toString();
	}
}
