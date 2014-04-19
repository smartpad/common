package com.jinnova.smartpad.drilling;

import static com.jinnova.smartpad.drilling.ActionLoad.*;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.db.CacheDao;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.IDetailManager;
import com.jinnova.smartpad.partner.Operation;


public class DetailManager implements IDetailManager {
	
	private static DetailDriller[] drillers = new DetailDriller[TYPE_COUNT];
	
	public static void initialize() {
		drillers[TYPE_BRANCH] = new DetailDriller() {
			
			@Override
			public void drill(String branchId, String gpsZone, 
					int page, int size, JsonObject resultJson) throws SQLException {
				
				DrillResult dr = new DrillResult();
				OperationDao odao = new OperationDao();
				String syscatId = odao.loadBranch(branchId).getSyscatId();

				//At most 5 stores belong to this branch and 3 similar branches
				dr.add(TYPENAME_COMPOUND_BRANCHSTORE, 
						new ALStoresBelongToBranch(branchId, 10, 8, 5), new ALBranchesBelongToSyscat(syscatId, branchId, 10, 8, 3));
				
				//5 active promotions from this branch in one compound 
				dr.add(new ALPromotionsBelongToBranch(branchId, 10, 5, 5)); //TODO multiple branches
				
				//10 sub categories of this branch's root category in one compound
				Object[] ja = CatalogDriller.findSubCatalogs(branchId, null, 10);
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
		};
		drillers[TYPE_STORE] = new StoreDriller();
	}
    
	@Override
    public String getDetail(int targetType, String targetId, String gpsLon, String gpsLat, int page, int size) throws SQLException {
    	
		String gpsZone = findGpsZone(gpsLon, gpsLat);
		String cached = CacheDao.query(targetType, targetId, gpsZone, page);
    	if (cached != null) {
    		return cached;
    	}
    	
    	JsonObject json = new JsonObject();
    	drillers[targetType].drill(targetId, gpsZone, page, size, json);
    	json.addProperty(FIELD_VERSION, "a");
    	//json.addProperty("page", page);
    	//json.addProperty("size", size);
    	cached = json.toString();
    	CacheDao.put(cached, targetType, targetId, gpsZone, page);
    	return cached;
    }
    
    public String getMore(String anchorType, String targetType, String relation, String anchorId,
    		String excludeId, String gpsLon, String gpsLat, int offset, int pageSize) throws SQLException {

    	return ActionLoad.loadMore(anchorType, targetType, relation, anchorId, excludeId, offset, pageSize).toString();
    }
	
	private String findGpsZone(String gpsLon, String gpsLat) {
		return null;
	}
}
