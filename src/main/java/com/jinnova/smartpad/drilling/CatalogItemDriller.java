package com.jinnova.smartpad.drilling;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.jinnova.smartpad.db.CatalogItemDao;
import com.jinnova.smartpad.db.DbIterator;
import com.jinnova.smartpad.partner.Catalog;
import com.jinnova.smartpad.partner.CatalogItem;
import com.jinnova.smartpad.partner.ICatalogSpec;

public class CatalogItemDriller {
	
	static JsonArray findCatalogItems(Catalog targetCatalog, String excludeCatItem, int count) throws SQLException {
		ICatalogSpec spec = targetCatalog.getSystemCatalog().getCatalogSpec(); //TODO exclude, count
		DbIterator<CatalogItem> catalogs = new CatalogItemDao().iterateCatalogItems(targetCatalog.getId(), targetCatalog.getSystemCatalogId(), spec);
		JsonArray ja = new JsonArray();
		while (catalogs.hasNext()) {
			CatalogItem one = catalogs.next();
			ja.add(one.generateFeedJson());
		}
		catalogs.close();
		return ja;
	}

}
