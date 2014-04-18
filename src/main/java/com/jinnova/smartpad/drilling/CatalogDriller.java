package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogSort;

import static com.jinnova.smartpad.partner.IDetailManager.*;
import static com.jinnova.smartpad.drilling.ActionLoad.*;

class CatalogDriller implements DetailDriller {

	/**
     * Returns in order the following:
     * 
     * 	- All sub categories of this category in one compound 
     * 	- Feature catalog items from this branch's root category
     * 	- All stores belong to this branch in one compound
     * 	- Some similar branches in one compound
     * 	- Some active promotions from this branch in one compound
     * 	- 2 posts from this branch
     *
     */
	@Override
	public void drill(String targetId, String gpsZone, int page, int size, JsonObject resultJson) throws SQLException {
		
		//5 sub cats, 3 sibling cats, 3 similar branches 
		Catalog cat = (Catalog) new CatalogDao().loadCatalog(targetId, false);
		Object[] ja = findSubCatalogs(targetId, null, 8);
		Object[] ja2 = findSubCatalogs(cat.getParentCatalogId(), targetId, 8);
		DrillResult dr = new DrillResult();
		dr.add(TYPENAME_COMPOUND_CAT, 
				new DrillSectionSimple(TYPENAME_CAT, ja, 5, new ActionLoad(TYPENAME_CAT, targetId, TYPENAME_CAT, REL_BELONG, 10)),
				new DrillSectionSimple(TYPENAME_CAT, ja2, 3, new ActionLoad(TYPENAME_CAT, targetId, TYPENAME_CAT, REL_SIBLING, 10)));
		
		//5 active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {cat.branchId}, 5);
		dr.add(TYPENAME_COMPOUND_PROMOS, ja, 5, new ActionLoad(TYPENAME_BRANCH, cat.branchId, TYPENAME_PROMO, REL_BELONG, 10)); //TODO multiple branches
		
		//5 feature items from this catalog
		ja = CatalogItemDriller.findCatalogItems(cat, null, 1, 5);
		dr.add(TYPENAME_COMPOUND_CITEM, ja, 5, new ActionLoad(TYPENAME_CAT, targetId, TYPENAME_CATITEM, REL_BELONG, 10));
		
		//5 other stores, 3 similar branches
		//ja = StoreDriller.findStoresOfBranch(cat.branchId, cat.storeId, 0, 8);
		ja = new OperationDao().iterateStores(cat.branchId, cat.storeId, 0, 8).toArray();
		OperationDao odao = new OperationDao();
		String syscatId = odao.loadBranch(cat.branchId).getSyscatId();
		ja2 = odao.iterateSimilarBranches(cat.branchId, syscatId).toArray();
		dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
				new DrillSectionSimple(TYPENAME_STORE, ja, 5, new ActionLoad(TYPENAME_STORE, cat.storeId, TYPENAME_STORE, REL_SIBLING, 10)),
				new DrillSectionSimple(TYPENAME_BRANCH, ja2, 3, new ActionLoad(TYPENAME_BRANCH, cat.branchId, TYPENAME_BRANCH, REL_SIMILAR, 10)));
		dr.writeJson(resultJson);
	}
	
	static Object[] findSubCatalogs(String parentCatId, String excludeCatId, int count) throws SQLException {
		/*DbIterator<Catalog> catalogs = new CatalogDao().iterateSubCatalogs(parentCatId, excludeCatId, count);
		JsonArray ja = new JsonArray();
		while (catalogs.hasNext()) {
			Catalog one = catalogs.next();
			ja.add(one.generateFeedJson());
		}
		catalogs.close();
		return ja;*/
		String syscatId = null; //don't need to parse spec 
		CachedPagingList<ICatalog, ICatalogSort> paging = Catalog.createSubCatalogPagingList(null, null, parentCatId, syscatId, null);
		paging.setPageSize(count);
		return paging.loadPage(1).getPageEntries();
		/*JsonArray ja = new JsonArray();
		for (ICatalog oneSub : subs) {
			ja.add(((Catalog) oneSub).generateFeedJson());
		}
		return ja;*/
	}
}
