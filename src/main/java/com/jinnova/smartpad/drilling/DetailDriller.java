package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonObject;

interface DetailDriller {
	
	void drill(String targetId, String gpsZone, int page, int size, JsonObject resultJson) throws SQLException;
}
