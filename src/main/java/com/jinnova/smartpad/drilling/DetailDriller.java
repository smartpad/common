package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;

interface DetailDriller {
	
	JsonArray generate(String targetId, String gpsZone, int page) throws SQLException;
}
