package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.ICatalogSort;
import com.jinnova.smartpad.partner.IDetailManager;

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
	public JsonArray generate(String targetId, String gpsZone, int page) throws SQLException {
		
		//5 sub cats, 3 sibling cats, 3 similar branches 
		Catalog cat = (Catalog) new CatalogDao().loadCatalog(targetId, false);
		Object[] ja = findSubCatalogs(targetId, null, 8);
		Object[] ja2 = findSubCatalogs(cat.getParentCatalogId(), targetId, 8);
		DrillResult dr = new DrillResult();
		dr.add(IDetailManager.TYPENAME_COMPOUND_CAT, ja, 5, ja2, 3);
		
		//5 active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {cat.branchId}, 5);
		dr.add(IDetailManager.TYPENAME_COMPOUND_PROMOS, ja, 5);
		
		//5 feature items from this catalog
		ja = CatalogItemDriller.findCatalogItems(cat, null, 5);
		dr.add(IDetailManager.TYPENAME_COMPOUND_CAT, ja, 5);
		
		//5 other stores, 3 similar branches
		ja = StoreDriller.findStoresOfBranch(cat.branchId, cat.storeId, 0, 8);
		ja2 = BranchDriller.findBranchesSimilar(cat.branchId, 8);
		dr.add(IDetailManager.TYPENAME_COMPOUND_BRANCHSTORE, ja, 5, ja2, 3);
		return dr.toJson();
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
