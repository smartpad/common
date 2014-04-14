package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.db.PromotionDao;
import com.jinnova.smartpad.partner.Promotion;

class PromotionDriller {

	static JsonArray findOperationPromotions(String[] branchIds) throws SQLException {
		DbIterator<Promotion> promos = new PromotionDao().iterateOperationPromos(branchIds);
		JsonArray ja = new JsonArray();
		while (promos.hasNext()) {
			Promotion one = promos.next();
			ja.add(one.generateFeedJson());
		}
		promos.close();
		return ja;
	}
}
