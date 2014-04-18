package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.db.PromotionDao;
import com.jinnova.smartpad.partner.IPromotion;
import com.jinnova.smartpad.partner.IPromotionSort;
import com.jinnova.smartpad.partner.Operation;

class PromotionDriller implements DetailDriller {
	
	@Override
	public void drill(String promoId, String gpsZone, int page, int size, JsonObject resultJson) throws SQLException {
		
		new PromotionDao().load(promoId);
		
		//At most 5 stores belong to this branch and 3 similar branches
		/*DrillResult dr = new DrillResult();
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
		return dr.toString();*/
	}

	static Object[] findOperationPromotions(String[] branchIds, int count) throws SQLException {
		CachedPagingList<IPromotion, IPromotionSort> paging = Operation.createPromotionPagingList(branchIds[0], /*operationId*/null, /*gps*/null); //TODO multiple branches
		paging.setPageSize(count);
		return paging.loadPage(1).getPageEntries();
		/*DbIterator<Promotion> promos = new PromotionDao().iterateOperationPromos(branchIds, count);
		JsonArray ja = new JsonArray();
		while (promos.hasNext()) {
			Promotion one = promos.next();
			ja.add(one.generateFeedJson());
		}
		promos.close();
		return ja;*/
	}
}
