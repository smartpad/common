package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
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
	public JsonArray generate(String branchId, String gpsZone, int page) throws SQLException {
		
		//At most 5 stores belong to this branch and 3 similar branches
		DrillResult dr = new DrillResult();
		OperationDao odao = new OperationDao();
		Object[] ja = odao.iterateStores(branchId, null, 0, 8).toArray();
		String syscatId = odao.loadBranch(branchId).getSyscatId();
		Object[] ja2 = odao.iterateSimilarBranches(branchId, syscatId).toArray();
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
		return dr.toJson();
	}
	
	/*static Object[] findBranchesSimilar(String targetBranchId, int count) throws SQLException {
		Operation targetBranch = (Operation) new OperationDao().loadBranch(targetBranchId);
		return findBranchesSimilar(targetBranch, count);
	}
	
	static Object[] findBranchesSimilar(Operation targetBranch, int count) throws SQLException {
		String syscatId = ((Catalog) targetBranch.getRootCatalog()).getSystemCatalogId();
		return new OperationDao().iterateSimilarBranches(targetBranch.getId(), syscatId).toArray();
	}*/
}
