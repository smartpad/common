package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.db.CatalogItemDao;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.ICatalogSpec;

public class CatalogItemDriller implements DetailDriller {
	
	@Override
	public JsonArray generate(String targetId, String gpsZone, int page) throws SQLException {
		
		//5 sibling cats, 3 similar branches 
		/*CatalogItem catItem = new CatalogItemDao().loadCatalogItem(targetId, spec);
		Catalog cat = (Catalog) new CatalogDao().loadCatalog(targetId, false);
		JsonArray ja = findSubCatalogs(targetId, null, 8);
		JsonArray ja2 = findSubCatalogs(cat.getParentCatalogId(), targetId, 8);
		DrillResult dr = new DrillResult();
		dr.add(IDetailManager.TYPENAME_CAT, ja, 5, ja2, 3);
		
		//5 active promotions from this branch in one compound
		ja = PromotionDriller.findOperationPromotions(new String[] {cat.branchId}, 5);
		dr.add(IDetailManager.TYPENAME_PROMO, ja, 5);
		
		//5 feature items from this catalog
		ja = CatalogItemDriller.findCatalogItems(cat, null, 5);
		dr.add(IDetailManager.TYPENAME_CATITEM, ja, 5);
		
		//5 other stores, 3 similar branches
		ja = StoreDriller.findStoresOfBranch(cat.branchId, cat.storeId, 0, 8);
		ja2 = BranchDriller.findBranchesSimilar(cat.branchId, 8);
		dr.add(IDetailManager.TYPENAME_COMPOUND_BRANCHSTORE, ja, 5, ja2, 3);
		return dr.toString();*/
		return null;
	}
	
	static JsonArray findCatalogItems(Catalog targetCatalog, String excludeCatItem, int count) throws SQLException {
		ICatalogSpec spec = targetCatalog.getSystemCatalog().getCatalogSpec(); //TODO exclude, count
		DbIterator<CatalogItem> catalogs = new CatalogItemDao().iterateCatalogItems(targetCatalog.getId(), targetCatalog.getSystemCatalogId(), spec);
		JsonArray ja = new JsonArray();
		while (catalogs.hasNext()) {
			CatalogItem one = catalogs.next();
			ja.add(one.generateFeedJson());
		}
		catalogs.close();
		return ja;
	}

}
