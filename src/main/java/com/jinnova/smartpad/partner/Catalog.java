package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.LinkedList;

import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.db.CatalogItemDao;

public class Catalog implements ICatalog {
	
	static final String CATALOG_ID_ROOT = "ROOT";
	
	private final String branchId;
	
	private String catalogId;
	
	private String parentCatalogId;
	
	private final Name name = new Name();

	private final LinkedList<ICatalog> subCatalogs = new LinkedList<>();

	private final LinkedList<ICatalogItem> catItems = new LinkedList<>();
	
	public Catalog(String branchId, String catalogId, String parentCatalogId) {
		this.branchId = branchId;
		this.catalogId = catalogId;
		this.parentCatalogId = parentCatalogId;
	}
	
	@Override
	public IName getName() {
		return this.name;
	}
	
	@Override
	public void loadChildren() throws SQLException {
		new CatalogDao().loadCatalogs(this.branchId, this.catalogId, this.subCatalogs);
		new CatalogItemDao().loadCatalogItems(this.catalogId, this.catItems);
	}

	@Override
	public ICatalog[] getSubCatalogs() {
		return subCatalogs.toArray(new ICatalog[subCatalogs.size()]);
	}

	@Override
	public ICatalogItem[] getItems() {
		return catItems.toArray(new ICatalogItem[catItems.size()]);
	}

	@Override
	public ICatalog newSubCatalogInstance() {
		return new Catalog(this.branchId, null, this.catalogId);
	}

	@Override
	public ICatalogItem newCatalogItemInstance() {
		return new CatalogItem(null);
	}

	@Override
	public void putSubCatalog(ICatalog subCatalog) throws SQLException {
		
		Catalog subCat = (Catalog) subCatalog;
		if (!this.catalogId.equals(subCat.parentCatalogId)) {
			throw new RuntimeException("Invalid catalog structure");
		}
		
		if (subCat.catalogId == null) {
			//creating new
			if (subCat.name.getName() == null || "".equals(subCat.name.getName().trim())) {
				throw new RuntimeException("Catalog's name unset");
			}
			String newId = SmartpadCommon.md5(subCat.branchId + subCat.name.getName());
			new CatalogDao().insert(subCat.branchId, newId, subCat.parentCatalogId, subCat);
			subCat.catalogId = newId;
			subCatalogs.add(subCat); 
		} else {
			//updating an existing
			new CatalogDao().update(subCat.catalogId, subCat);
		}
	}

	@Override
	public void putCatalogItem(ICatalogItem catItem) throws SQLException {
		CatalogItem item = (CatalogItem) catItem;
		/*if (!this.catalogId.equals(item.catalogId)) {
			throw new RuntimeException("Invalid catalog item structure");
		}*/
		
		if (item.getItemId() == null) {
			//creating new
			if (item.getName().getName() == null || "".equals(item.getName().getName().trim())) {
				throw new RuntimeException("CatalogItem's name unset");
			}
			String newId = SmartpadCommon.md5(this.branchId + this.catalogId + item.getName().getName());
			new CatalogItemDao().insert(this.branchId, newId, this.catalogId, item);
			item.setItemId(newId);
			catItems.add(item); 
		} else {
			//updating an existing
			new CatalogItemDao().update(item.getItemId(), item);
		}
	}

	@Override
	public void delete(ICatalog cat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(ICatalogItem item) {
		// TODO Auto-generated method stub
		
	}
}
