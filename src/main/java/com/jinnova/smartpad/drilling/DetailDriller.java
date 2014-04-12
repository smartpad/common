package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

interface DetailDriller {
	
	String generate(String targetId, String gpsZone, int page) throws SQLException;
}
