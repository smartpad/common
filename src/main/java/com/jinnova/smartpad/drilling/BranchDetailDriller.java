package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.db.OperationDao;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.Operation;

class BranchDetailDriller implements DetailDriller {

	@Override
	public String generate(String targetId, String gpsZone, int page) throws SQLException {
		DbIterator<Operation> it = new OperationDao().iterateStores(targetId);
		JsonArray ja = new JsonArray();
		while (it.hasNext()) {
			IOperation one = it.next();
			JsonObject json = new JsonObject();
			json.addProperty("id", one.getId());
			json.addProperty("type", "store");
			json.addProperty("name", one.getName().getName());
			ja.add(json);
		}
		
		JsonObject branchJson = new JsonObject();
		branchJson.add("stores", ja);
		return branchJson.toString();
	}
}
