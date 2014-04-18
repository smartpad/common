package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.Operation;

import static com.jinnova.smartpad.partner.IDetailManager.*;
import static com.jinnova.smartpad.drilling.ActionLoad.*;

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
	public void drill(String branchId, String gpsZone, int page, int size, JsonObject resultJson) throws SQLException {
		
		DrillResult dr = new DrillResult();
		OperationDao odao = new OperationDao();
		String syscatId = odao.loadBranch(branchId).getSyscatId();

		//At most 5 stores belong to this branch and 3 similar branches
		Object[] ja = odao.iterateStores(branchId, null, 0, 8).toArray();
		Object[] ja2 = odao.iterateSimilarBranches(branchId, syscatId).toArray();
		dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
				new DrillSectionSimple(TYPENAME_STORE, ja, 5, new ActionLoad(TYPENAME_BRANCH, branchId, TYPENAME_STORE, REL_BELONG, 10)), 
				new DrillSectionSimple(TYPENAME_BRANCH, ja2, 3, new ActionLoad(TYPENAME_BRANCH, branchId, TYPENAME_BRANCH, REL_SIMILAR, 10)));
		
		//5 active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {branchId}, 5);
		dr.add(TYPENAME_COMPOUND_PROMOS, ja, 5, new ActionLoad(TYPENAME_BRANCH, branchId, TYPENAME_PROMO, REL_BELONG, 10)); //TODO multiple branches
		
		//10 sub categories of this branch's root category in one compound
		ja = CatalogDriller.findSubCatalogs(branchId, null, 10);
		dr.add(TYPENAME_COMPOUND_CAT, ja, 10, new ActionLoad(TYPENAME_CAT, branchId, TYPENAME_CAT, REL_BELONG, 10));
		
		//catelog items from this branch's root category
		Operation targetBranch = (Operation) new OperationDao().loadBranch(branchId);
		Catalog rootCat = (Catalog) targetBranch.getRootCatalog();
		ja = CatalogItemDriller.findCatalogItems(rootCat, null, page, size);
		dr.add(TYPENAME_COMPOUND_CITEM, ja, 20, new ActionLoad(TYPENAME_CAT, branchId, TYPENAME_CATITEM, REL_BELONG, 10));
		
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//return gson.toJson(dr);
		dr.writeJson(resultJson);
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
