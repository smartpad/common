package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.db.CatalogItemDao;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogItem;

public class CatalogItemDriller {
	
	static JsonArray findCatalogItems(Catalog targetCatalog) throws SQLException {
		DbIterator<CatalogItem> catalogs = new CatalogItemDao(targetCatalog).iterateCatalogItems();
		JsonArray ja = new JsonArray();
		while (catalogs.hasNext()) {
			CatalogItem one = catalogs.next();
			ja.add(one.generateFeedJson());
		}
		catalogs.close();
		return ja;
	}

}
