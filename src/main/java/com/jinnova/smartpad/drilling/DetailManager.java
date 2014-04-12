package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.jinnova.smartpad.db.CacheDao;
import com.jinnova.smartpad.partner.IDetailManager;

public class DetailManager implements IDetailManager {
	
	private static DetailDriller[] drillers = new DetailDriller[TYPE_COUNT];
	
	public static void initialize() {
		drillers[TYPE_BRANCH] = new BranchDetailDriller();
		drillers[TYPE_STORE] = new StoreDetailDriller();
	}
    
	@Override
    public String getDetail(int targetType, String targetId, String gpsLon, String gpsLat, int page) throws SQLException {
    	
		String gpsZone = findGpsZone(gpsLon, gpsLat);
		String cached = CacheDao.query(targetType, targetId, gpsZone, page);
    	if (cached != null) {
    		return cached;
    	}
    	cached = drillers[targetType].generate(targetId, gpsZone, page);
    	CacheDao.put(cached, targetType, targetId, gpsZone, page);
    	return cached;
    }
	
	private String findGpsZone(String gpsLon, String gpsLat) {
		return null;
	}
}
