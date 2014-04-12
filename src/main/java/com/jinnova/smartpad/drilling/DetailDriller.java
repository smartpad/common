package com.jinnova.smartpad.drilling;

import java.sql.Connection;
import java.sql.SQLException;

interface DetailDriller {
	
	String generate(Connection conn, String targetId, String gpsZone, int page) throws SQLException;
}
