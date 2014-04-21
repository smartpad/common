package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

interface DetailDriller {
	
	DrillResult drill(String targetId, String gpsZone, int page, int size) throws SQLException;
}
