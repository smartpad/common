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
		
		//At most 5 stores belong to this branch and 3 similar branches
		DrillResult dr = new DrillResult();
		JsonArray ja = StoreDriller.findStoresOfBranch(branchId, null, 0, 8);
		JsonArray ja2 = findBranchesSimilar(branchId, 8);
		dr.add(IDetailManager.TYPENAME_COMPOUND_BRANCHSTORE, ja, 5, ja2, 3);
		
		//5 active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {branchId}, 5);
		dr.add(IDetailManager.TYPENAME_COMPOUND_PROMOS, ja, 5);
		
		//10 sub categories of this branch's root category in one compound
		ja = CatalogDriller.findSubCatalogs(branchId, null, 10);
		dr.add(IDetailManager.TYPENAME_COMPOUND_CAT, ja, 10);
		
		//Feature catelog items from this branch's root category
		Operation targetBranch = (Operation) new OperationDao().loadBranch(branchId);
		ja = CatalogItemDriller.findCatalogItems((Catalog) targetBranch.getRootCatalog(), null, 20);
		dr.add(IDetailManager.TYPENAME_COMPOUND_CITEM, ja, 20);
		
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//return gson.toJson(dr);
		return dr.toString();
	}
	
	static JsonArray findBranchesSimilar(String targetBranchId, int count) throws SQLException { //TODO count
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
