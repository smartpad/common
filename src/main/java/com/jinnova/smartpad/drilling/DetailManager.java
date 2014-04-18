package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.db.CacheDao;

import com.jinnova.smartpad.partner.IDetailManager;


public class DetailManager implements IDetailManager {
	
	private static DetailDriller[] drillers = new DetailDriller[TYPE_COUNT];
	
	public static void initialize() {
		drillers[TYPE_BRANCH] = new BranchDriller();
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
	
	private String findGpsZone(String gpsLon, String gpsLat) {
		return null;
	}
}
