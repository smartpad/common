package com.jinnova.smartpad.drilling;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.partner.IOperation;
import com.jinnova.smartpad.partner.PartnerManager;
import com.jinnova.smartpad.partner.User;

class BranchDetailDriller implements DetailDriller {

	@Override
	public String generate(Connection conn, String targetId, String gpsZone, int page) throws SQLException {

		User user = (User) PartnerManager.instance.getSystemUser();
		user.getStorePagingList().setPageSize(-1);
		IOperation[] stores = user.getStorePagingList().loadPage(user, 1).getPageItems();
		JsonArray ja = new JsonArray();
		for (IOperation one : stores) {
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
