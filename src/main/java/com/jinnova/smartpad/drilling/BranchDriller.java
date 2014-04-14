package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.IDetailManager;
import com.jinnova.smartpad.partner.Operation;

class BranchDriller implements DetailDriller {

	/**
     * Returns in order the following:
     * 
     *  - Extra details of this branch
     * 	- 5 stores belong to this branch and/or some similar branches 
     * 	- 5 active promotions from this branch and/or its stores
     * 	- All sub categories of this branch's root category
     * 	- 3 posts from this branch 
     * 	- Top 20 catalog items from this branch's root category
     *
     */
	@Override
	public String generate(String branchId, String gpsZone, int page) throws SQLException {
		
		//All stores belong to this branch in one compound
		JsonArray branchJson = new JsonArray();
		JsonArray ja = StoreDriller.findStores(branchId, null);
		branchJson.add(CompoundFeed.generateFeedJson(IDetailManager.TYPENAME_COMPOUND, ja));
		
		//Some similar branches in one compound
		ja = findBranches(branchId);
		branchJson.add(CompoundFeed.generateFeedJson(IDetailManager.TYPENAME_COMPOUND, ja));
		
		//Some active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {branchId});
		branchJson.add(CompoundFeed.generateFeedJson(IDetailManager.TYPENAME_COMPOUND, ja));
		
		//All sub categories of this branch's root category in one compound
		ja = CatalogDriller.findSubCataogs(branchId);
		branchJson.add(CompoundFeed.generateFeedJson(IDetailManager.TYPENAME_COMPOUND, ja));
		
		//Feature catelog items from this branch's root category
		Operation targetBranch = (Operation) new OperationDao().loadBranch(branchId);
		ja = CatalogItemDriller.findCatalogItems((Catalog) targetBranch.getRootCatalog());
		branchJson.add(CompoundFeed.generateFeedJson(IDetailManager.TYPENAME_COMPOUND, ja));
		
		return branchJson.toString();
	}
	
	static JsonArray findBranches(String targetBranchId) throws SQLException {
		Operation targetBranch = (Operation) new OperationDao().loadBranch(targetBranchId);
		String syscatId = ((Catalog) targetBranch.getRootCatalog()).getSystemCatalogId();
		DbIterator<Operation> similarBranches = new OperationDao().iterateSimilarBranches(targetBranchId, syscatId);
		JsonArray ja = new JsonArray();
		while (similarBranches.hasNext()) {
			Operation one = similarBranches.next();
			ja.add(one.generateFeedJson());
		}
		similarBranches.close();
		return ja;
	}
}
