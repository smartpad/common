package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.CachedPage;
import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.PageEntrySupport;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.db.CatalogItemDao;

public class Catalog implements ICatalog, Feed {
	
	public final String branchId;
	
	public final String storeId;
	
	private String catalogId;
	
	private final CatalogSpec catalogSpec;
	
	private String parentCatalogId;
	
	private String systemCatalogId;
	
	public final GPSInfo gps = new GPSInfo();
	
	private final Name name = new Name();
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private CachedPagingList<ICatalog, ICatalogSort> subCatalogPagingList;
	
	private CachedPagingList<ICatalogItem, ICatalogItemSort> catalogItemPagingList;
	
	public Catalog(String branchId, String storeId, String catalogId, String parentCatalogId, String systemCatalogId) {
		/*if (storeId == null) {
			throw new NullPointerException();
		}*/
		this.branchId = branchId;
		this.storeId = storeId;
		this.catalogId = catalogId;
		this.parentCatalogId = parentCatalogId;
		if (systemCatalogId == null) {
			this.catalogSpec = new CatalogSpec();
		} else {
			this.catalogSpec = null;
			this.systemCatalogId = systemCatalogId;
		}
		createPagingLists();
	}
	
	private void createPagingLists() {
		subCatalogPagingList = createSubCatalogPagingList(branchId, storeId, catalogId, systemCatalogId, gps);
		catalogItemPagingList = createCatalogItemPagingList(branchId, storeId, catalogId, systemCatalogId, gps);
	}
	
	public static CachedPagingList<ICatalog, ICatalogSort> createSubCatalogPagingList(
			final String branchId, final String storeId, final String catalogId, final String systemCatalogId, final GPSInfo gps) {
		
		PageEntrySupport<ICatalog, ICatalogSort> subCatalogSupport = new PageEntrySupport<ICatalog, ICatalogSort>() {
			
			@Override
			public ICatalog newEntryInstance(IUser authorizedUser) {
				Catalog subCat = new Catalog(branchId, storeId, null, catalogId, systemCatalogId);
				subCat.gps.inherit(gps, null);
				return subCat;
			}
			
			@Override
			public boolean isPersisted(ICatalog member) {
				return ((Catalog) member).catalogId != null;
			}
			
			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new CatalogDao().countSubCatalogs(catalogId);
			}
			
			@Override
			public LinkedList<ICatalog> load(IUser authorizedUser, int offset,
					int pageSize, ICatalogSort sortField, boolean ascending) throws SQLException {
				
				boolean parseSpec = systemCatalogId == null;
				return new CatalogDao().loadSubCatalogs(catalogId, offset, pageSize, sortField, ascending, parseSpec);
			}
			
			@Override
			public void insert(IUser authorizedUser, ICatalog newMember) throws SQLException {
				Catalog subCat = (Catalog) newMember;
				if (subCat.name.getName() == null || "".equals(subCat.name.getName().trim())) {
					throw new RuntimeException("Catalog's name unset");
				}
				if (subCat.catalogSpec != null && subCat.systemCatalogId != null) {
					throw new RuntimeException("A system catalog must not linked to any other system catalog");
				}
				
				if (subCat.catalogSpec == null && subCat.systemCatalogId == null) {
					throw new RuntimeException("This catalog must be linked to a system catalog");
				}
				
				String newId;
				if (subCat.catalogSpec == null) {
					//newId = SmartpadCommon.md5(subCat.branchId + subCat.name.getName());
					newId = catalogId + "_1";
				} else {
					newId = subCat.catalogSpec.getSpecId();
					if (newId.contains(" ")) {
						throw new RuntimeException("CatalogSpec id can't contains special charaters");
					}
				}
				new CatalogDao().insert(subCat.branchId, subCat.storeId, newId, subCat.parentCatalogId, subCat);
				subCat.catalogId = newId;
				subCat.createPagingLists();
				
				if (subCat.catalogSpec != null) {
					PartnerManager.instance.putSystemCatalog(subCat);
				}
			}
			
			@Override
			public void update(IUser authorizedUser, ICatalog member) throws SQLException {
				Catalog subCat = (Catalog) member;
				new CatalogDao().update(subCat.catalogId, subCat);
			}
			
			@Override
			public void delete(IUser authorizedUser, ICatalog member) throws SQLException {
				Catalog subCat = (Catalog) member;
				new CatalogDao().delete(subCat.catalogId);
			}
		};

		@SuppressWarnings("unchecked")
		Comparator<ICatalog>[] subCatalogComparators = new Comparator[ICatalogSort.values().length];
		subCatalogComparators[ICatalogSort.createDate.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o1.getRecordInfo().getCreateDate());
			}
		};
		subCatalogComparators[ICatalogSort.updateDate.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		subCatalogComparators[ICatalogSort.createBy.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getCreateBy().compareTo(o2.getRecordInfo().getCreateBy());
			}
		};
		subCatalogComparators[ICatalogSort.updateBy.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getUpdateBy().compareTo(o2.getRecordInfo().getUpdateBy());
			}
		};
		subCatalogComparators[ICatalogSort.name.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getName().getName().compareTo(o2.getName().getName());
			}
		};
		
		return new CachedPagingList<ICatalog, ICatalogSort>(
				subCatalogSupport, subCatalogComparators, ICatalogSort.createDate, new Catalog[0]);
	}
	
	public static CachedPagingList<ICatalogItem, ICatalogItemSort> createCatalogItemPagingList(
			final String branchId, final String storeId, final String catalogId, final String systemCatalogId, final GPSInfo gps) {
		PageEntrySupport<ICatalogItem, ICatalogItemSort> catalogItemSupport = new PageEntrySupport<ICatalogItem, ICatalogItemSort>() {
			

			@Override
			public ICatalogItem newEntryInstance(IUser authorizedUser) {
				CatalogItem ci = new CatalogItem(branchId, storeId, catalogId, systemCatalogId, null);
				ci.gps.inherit(gps, gps.getInheritFrom());
				return ci;
			}
			
			@Override
			public boolean isPersisted(ICatalogItem member) {
				return ((CatalogItem) member).getId() != null;
			}
			
			@Override
			public int count(IUser authorizedUser) throws SQLException {
				ICatalog syscat = PartnerManager.instance.getSystemCatalog(systemCatalogId);
				return new CatalogItemDao().countCatalogItems(catalogId, syscat.getCatalogSpec());
			}
			
			@Override
			public LinkedList<ICatalogItem> load(IUser authorizedUser, int offset,
					int pageSize, ICatalogItemSort sortField, boolean ascending) throws SQLException {

				ICatalog syscat = PartnerManager.instance.getSystemCatalog(systemCatalogId);
				return new CatalogItemDao().loadCatalogItems(
						catalogId, syscat.getCatalogSpec(), offset, pageSize, sortField, ascending);
			}
			
			@Override
			public void insert(IUser authorizedUser, ICatalogItem newMember) throws SQLException {
				CatalogItem item = (CatalogItem) newMember;
				String name = item.getFieldValue(ICatalogField.ID_NAME);
				if (name == null || "".equals(name.trim())) {
					throw new RuntimeException("CatalogItem's name unset");
				}
				String newId = SmartpadCommon.md5(branchId + catalogId + name);
				ICatalog syscat = PartnerManager.instance.getSystemCatalog(systemCatalogId);
				new CatalogItemDao().insert(newId, syscat.getCatalogSpec(), item);
				item.setId(newId);
			}
			
			@Override
			public void update(IUser authorizedUser, ICatalogItem member) throws SQLException {
				CatalogItem item = (CatalogItem) member;
				ICatalog syscat = PartnerManager.instance.getSystemCatalog(systemCatalogId);
				new CatalogItemDao().update(item.getId(), syscat.getCatalogSpec(), item);
			}
			
			@Override
			public void delete(IUser authorizedUser, ICatalogItem member) throws SQLException {
				CatalogItem item = (CatalogItem) member;
				new CatalogItemDao().delete(item.getId());
			}
		};
		
		@SuppressWarnings("unchecked")
		Comparator<ICatalogItem>[] catalogItemComparators = new Comparator[ICatalogItemSort.values().length];		
		catalogItemComparators[ICatalogItemSort.createDate.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o2.getRecordInfo().getCreateDate());
			}
		};
		catalogItemComparators[ICatalogItemSort.updateDate.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		catalogItemComparators[ICatalogItemSort.createBy.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getCreateBy().compareTo(o2.getRecordInfo().getCreateBy());
			}
		};
		catalogItemComparators[ICatalogItemSort.updateBy.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getUpdateBy().compareTo(o2.getRecordInfo().getUpdateBy());
			}
		};
		catalogItemComparators[ICatalogItemSort.name.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return StringArrayUtils.compare(o1, o2, ICatalogField.ID_NAME);
			}
		};
		
		return new CachedPagingList<ICatalogItem, ICatalogItemSort>(
				catalogItemSupport, catalogItemComparators, ICatalogItemSort.createDate, new CatalogItem[0]);
	}
	
	@Override
	public String getId() {
		return this.catalogId;
	}

	@Override
	public ICatalog getSystemCatalog() {
		return PartnerManager.instance.getSystemCatalog(this.systemCatalogId);
	}
	
	public String getSystemCatalogId() {
		return this.systemCatalogId;
	}
	
	@Override
	public void setSystemCatalogId(String systemCatalogId) {
		this.systemCatalogId = systemCatalogId;
	}
	
	public String getParentCatalogId() {
		return this.parentCatalogId;
	}

	@Override
	public IName getName() {
		return this.name;
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}

	@Override
	public ICatalogSpec getCatalogSpec() {
		return catalogSpec;
	}

	@Override
	public IPagingList<ICatalog, ICatalogSort> getSubCatalogPagingList() {
		return subCatalogPagingList;
	}

	@Override
	public IPagingList<ICatalogItem, ICatalogItemSort> getCatalogItemPagingList() {
		return catalogItemPagingList;
	}

	void loadAllSubCatalogsRecursively(HashMap<String, Catalog> catMap) throws SQLException {
		LinkedList<Catalog> catList = new LinkedList<Catalog>();
		catList.add(this);
		loadAllSubCatalogsRecursively(catMap, catList);
	}

	private static void loadAllSubCatalogsRecursively(HashMap<String, Catalog> catMap, LinkedList<Catalog> catList) throws SQLException {
		while (!catList.isEmpty()) {
			Catalog oneCat = catList.remove();
			catMap.put(oneCat.catalogId, oneCat);
			oneCat.subCatalogPagingList.setPageSize(-1); //load all
			CachedPage<ICatalog> page = oneCat.subCatalogPagingList.loadPage(PartnerManager.instance.systemUser, 1);
			for (ICatalog sub : page.getPageEntries()) {
				catList.add((Catalog) sub);
			}
		}
	}

	public JsonObject generateFeedJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", this.catalogId);
		json.addProperty("type", IDetailManager.TYPENAME_CAT);
		json.addProperty("name", this.name.getName());
		return json;
	}
}
