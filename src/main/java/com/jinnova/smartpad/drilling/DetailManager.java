package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.db.CacheDao;
import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.db.CatalogItemDao;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.ICatalog;
import com.jinnova.smartpad.partner.IDetailManager;
import com.jinnova.smartpad.partner.Operation;
import com.jinnova.smartpad.partner.PartnerManager;

public class DetailManager implements IDetailManager {
	
	private static DetailDriller[] drillers = new DetailDriller[TYPE_COUNT];
    
	@Override
    public String drill(String targetType, String targetId, String gpsLon, String gpsLat/*, int page, int size*/) throws SQLException {
    	
		int targetTypeNumber = typeNameToNumber(targetType);
		String gpsZone = findGpsZone(gpsLon, gpsLat);
		String cached = CacheDao.query(targetTypeNumber, targetId, gpsZone/*, page*/);
    	if (cached != null) {
    		return cached;
    	}
    	
    	DrillResult dr = drillers[targetTypeNumber].drill(targetId, gpsZone/*, page, size*/);
    	JsonObject json = new JsonObject();
    	dr.writeJson(json);
    	json.addProperty(FIELD_VERSION, "a");
    	//json.addProperty("page", page);
    	//json.addProperty("size", size);
    	cached = json.toString();
    	CacheDao.put(cached, targetTypeNumber, targetId, gpsZone/*, page*/);
    	return cached;
    }
	
	private String findGpsZone(String gpsLon, String gpsLat) {
		return null;
	}

	@Override
	public String more(String targetType, String anchorType, String anchorId, String relation,
			String branchId, String storeId, String catId, String syscatId, String excludeId,
			String gpsLon, String gpsLat, int offset, int size) throws SQLException {
		
		//Object[] ja
		ActionLoad action = ActionLoad.loadMore(targetType, anchorType, anchorId, relation, branchId, storeId, catId, syscatId, excludeId, gpsLon, gpsLat, offset, size);

		/*if (ja == null || ja.length == 0) {
			return null;
		}
		if (ja.length == 1) {
			return ((Feed) ja[0]).generateFeedJson().toString();
		}*/
		
		Object[] data = action.load();
		JsonArray array = new JsonArray();
		for (int i = 0; i < data.length; i++) {
			array.add(((Feed) data[i]).generateFeedJson());
		}
		
		JsonObject json = new JsonObject();
		json.add(IDetailManager.FIELD_ARRAY, array);
		json.addProperty(IDetailManager.FIELD_ACTION_LOADNEXT, action.generateNextLoadUrl());
		//System.out.println("next load: " + actionLoad.generateNextLoadUrl());
		return json.toString();
	}
	
	public static void initialize() {
		drillers[TYPE_NO] = new DetailDriller() {
			
			@Override
			public DrillResult drill(String branchId, String gpsZone/*, int page, int size*/) throws SQLException {
				
				DrillResult dr = new DrillResult();
				ICatalog rootSyscat = PartnerManager.instance.getSystemRootCatalog();
				for (ICatalog cat : PartnerManager.instance.getSystemSubCatalog(rootSyscat.getId())) {
					dr.add(new ALItemBelongRecursivelyToSyscat(cat.getId(), 10, 10, 10));
				}
				return dr;
			}
		};
		drillers[TYPE_BRANCH] = new DetailDriller() {
			
			@Override
			public DrillResult drill(String branchId, String gpsZone/*, int page, int size*/) throws SQLException {
				
				DrillResult dr = new DrillResult();
				OperationDao odao = new OperationDao();
				String syscatId = odao.loadBranch(branchId).getSyscatId();

				//At most 5 stores belong to this branch and 3 similar branches
				dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
						new ALStoresBelongToBranch(branchId, null, 10, 8, 5), new ALBranchesBelongDirectlyToSyscat(syscatId, branchId, 10, 8, 3));
				
				//5 active promotions by syscat, this branch first 
				dr.add(new ALPromotionsBelongDirectlyToSyscat(syscatId, branchId, 10, 5, 5));
				
				//10 sub categories of this branch's root category in one compound
				dr.add(new ALCatalogsBelongDirectlyToCatalog(branchId, null, 10, 10, 10));
				
				//catelog items from this branch's root category
				dr.add(new ALItemBelongDirectlyToCatalog(branchId, syscatId, 20, 20, 20));
				return dr;
			}
		};
		drillers[TYPE_STORE] = new DetailDriller() {

			@Override
			public DrillResult drill(String targetId, String gpsZone/*, int page, int size*/) throws SQLException {
				
				DrillResult dr = new DrillResult();
				Operation targetStore = new OperationDao().loadStore(targetId);
				
				//5 stores belong in same branch with this store, and 3 similar branches
				dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
						new ALStoresBelongToBranch(targetStore.getBranchId(), targetId, 10, 8, 5), 
						new ALBranchesBelongDirectlyToSyscat(targetStore.getSyscatId(), targetStore.getBranchId(), 10, 8, 3));
				
				//Some active promotions from this branch in one compound
				dr.add(new ALPromotionsBelongDirectlyToSyscat(targetStore.getSyscatId(), targetStore.getBranchId(), 10, 10, 10));
				return dr;
			}
			
		};
		drillers[TYPE_CAT] = new DetailDriller() {

			@Override
			public DrillResult drill(String targetId, String gpsZone/*, int page, int size*/) throws SQLException {
				
				//5 sub cats, 3 sibling cats 
				Catalog cat = (Catalog) new CatalogDao().loadCatalog(targetId, false);
				DrillResult dr = new DrillResult();
				dr.add(TYPENAME_COMPOUND, 
						new ALCatalogsBelongDirectlyToCatalog(targetId, null, 10, 8, 5), 
						new ALCatalogsBelongDirectlyToCatalog(cat.getParentCatalogId(), targetId, 10, 8, 3));
				
				//5 active promotions from this branch in one compound
				dr.add(new ALPromotionsBelongDirectlyToSyscat(cat.getSystemCatalogId(), cat.branchId, 10, 5, 5));
				
				//5 feature items from this catalog
				dr.add(new ALItemBelongDirectlyToCatalog(targetId, cat.getSystemCatalogId(), 10, 5, 5));
				
				//5 other stores, 3 similar branches
				//ja = StoreDriller.findStoresOfBranch(cat.branchId, cat.storeId, 0, 8);
				dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
						new ALStoresBelongToBranch(cat.branchId, cat.storeId, 10, 8, 5), 
						new ALBranchesBelongDirectlyToSyscat(cat.getSystemCatalogId(), cat.branchId, 10, 8, 3));
				return dr;
			}
			
		};
		
		drillers[TYPE_CATITEM] = new DetailDriller() {
			
			@Override
			public DrillResult drill(String targetId, String gpsZone/*, int page, int size*/) throws SQLException {
				//5 sibling cats, 3 similar branches 
				CatalogItem catItem = new CatalogItemDao().loadCatalogItem(targetId, null);
				Catalog cat = new CatalogDao().loadCatalog(catItem.getCatalogId(), false);
				DrillResult dr = new DrillResult();
				dr.add(new ALCatalogsBelongDirectlyToCatalog(cat.getParentCatalogId(), catItem.getCatalogId(), 10, 8, 5));
				return dr;
				/*Catalog cat = (Catalog) new CatalogDao().loadCatalog(targetId, false);
				JsonArray ja = findSubCatalogs(targetId, null, 8);
				JsonArray ja2 = findSubCatalogs(cat.getParentCatalogId(), targetId, 8);
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
			}
		};
		
		drillers[TYPE_PROMO] = new DetailDriller() {
			
			@Override
			public DrillResult drill(String targetId, String gpsZone/*, int page, int size*/) throws SQLException {

				
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
				return null;
			}
		};
	}
	
	private static int typeNameToNumber(String name) {
		
		if (TYPENAME_NO == name) {
			return TYPE_NO;
		} else if (TYPENAME_BRANCH.equals(name)) {
			return TYPE_BRANCH;
		} else if (TYPENAME_STORE.equals(name)) {
			return TYPE_STORE;
		} else if (TYPENAME_CAT.equals(name)) {
			return TYPE_CAT;
		} else if (TYPENAME_CATITEM.equals(name)) {
			return TYPE_CATITEM;
		} else if (TYPENAME_PROMO.equals(name)) {
			return TYPE_PROMO;
		} else {
			throw new RuntimeException();
		}
	}
}
