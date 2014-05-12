package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.*;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.JsonArray;
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
import com.jinnova.smartpad.db.OperationDao;

public class Catalog implements ICatalog, Feed {
	
	public final String branchId;
	
	public final String storeId;
	
	private String catalogId;
	
	private final CatalogSpec catalogSpec;
	
	private String parentCatalogId;
	
	private String systemCatalogId;
	
	private String branchName;
	
	private String parentCatName;
	
	public final GPSInfo gps = new GPSInfo();
	
	private final Name name = new Name();
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private CachedPagingList<ICatalog, ICatalogSort> subCatalogPagingList;
	
	private CachedPagingList<ICatalogItem, ICatalogItemSort> catalogItemPagingList;
	
	private boolean createCatItemClusterTable = false;
	
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
		//String syscatId = systemCatalogId;
		subCatalogPagingList = createSubCatalogPagingList(branchId, storeId, catalogId, systemCatalogId, branchName, name.getName(), gps, createCatItemClusterTable);
		
		//this is for syscat to have system items
		String syscatId = systemCatalogId != null ? systemCatalogId : catalogSpec.getSpecId();
		catalogItemPagingList = createCatalogItemPagingList(branchId, storeId, catalogId, syscatId, branchName, name.getName(), gps);
	}
	
	public void setCreateCatItemClusterTable() {
		this.createCatItemClusterTable = true;
		//String syscatId = systemCatalogId != null ? systemCatalogId : catalogSpec.getSpecId();
		//String syscatId = systemCatalogId;
		subCatalogPagingList = createSubCatalogPagingList(branchId, storeId, catalogId, systemCatalogId, branchName, name.getName(), gps, createCatItemClusterTable);
	}
	
	/*public static CachedPagingList<ICatalog, ICatalogSort> createSubCatalogPagingList(final String branchId, final String storeId, 
			final String catalogId, final String systemCatalogId, final String branchName, final GPSInfo gps) {
		
		return createSubCatalogPagingList(branchId, storeId, catalogId, systemCatalogId, branchName, gps, false);
	}*/
	
	private static CachedPagingList<ICatalog, ICatalogSort> createSubCatalogPagingList(final String branchId, final String storeId, 
			final String catalogId, final String systemCatalogId, final String branchName, final String catalogName, 
			final GPSInfo gps, final boolean createCatItemClusterTable) {
		
		PageEntrySupport<ICatalog, ICatalogSort> subCatalogSupport = new PageEntrySupport<ICatalog, ICatalogSort>() {
			
			@Override
			public ICatalog newEntryInstance(IUser authorizedUser) {
				Catalog subCat = new Catalog(branchId, storeId, null, catalogId, systemCatalogId);
				subCat.setBranchName(branchName);
				subCat.setParentCatName(catalogName);
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
				
				String newIdPrefix;
				String[] newIdGen = new String[1];
				if (subCat.catalogSpec == null) {
					//newIdPrefix = SmartpadCommon.md5(catalogId + subCat.name.getName());
					newIdPrefix = catalogId;
				} else {
					//newIdPrefix = subCat.catalogSpec.getSpecId();
					String providedSyscatId = subCat.catalogSpec.getSpecId();
					if (providedSyscatId.contains(" ") || providedSyscatId.contains("_")) {
						throw new RuntimeException("CatalogSpec id can't contains special charaters");
					}
					newIdPrefix = null;
					if (IDetailManager.SYSTEM_BRANCH_ID.equals(catalogId)) {
						newIdGen[0] = providedSyscatId;
					} else {
						newIdGen[0] = catalogId + "_" + providedSyscatId;
					}
					subCat.getCatalogSpec().setSpecId(newIdGen[0]);
				}
				new CatalogDao().insert(subCat.branchId, subCat.storeId, newIdPrefix, newIdGen, subCat.parentCatalogId, subCat, createCatItemClusterTable);
				subCat.catalogId = newIdGen[0];
				subCat.createPagingLists();
				
				if (subCat.catalogSpec != null) {
					PartnerManager.instance.putSystemCatalog(subCat);
				}
			}
			
			@Override
			public void update(IUser authorizedUser, ICatalog member) throws SQLException {
				Catalog subCat = (Catalog) member;
				new CatalogDao().update(subCat.catalogId, subCat);
				//TODO need to alter item table
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
				return o1.getName().compareTo(o2.getName());
			}
		};
		
		return new CachedPagingList<ICatalog, ICatalogSort>(
				subCatalogSupport, subCatalogComparators, ICatalogSort.createDate, new Catalog[0]);
	}
	
	public static CachedPagingList<ICatalogItem, ICatalogItemSort> createCatalogItemPagingList(
			final String branchId, final String storeId, final String catalogId, final String systemCatalogId, 
			final String branchName, final String catName, final GPSInfo gps) {
		PageEntrySupport<ICatalogItem, ICatalogItemSort> catalogItemSupport = new PageEntrySupport<ICatalogItem, ICatalogItemSort>() {
			

			@Override
			public ICatalogItem newEntryInstance(IUser authorizedUser) {
				CatalogItem ci = new CatalogItem(branchId, storeId, catalogId, systemCatalogId, null);
				ci.setBranchName(branchName);
				ci.setCatalogName(catName);
				ci.gps.inherit(gps, gps.getInheritFrom());
				return ci;
			}
			
			@Override
			public boolean isPersisted(ICatalogItem member) {
				return ((CatalogItem) member).getId() != null;
			}
			
			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new CatalogItemDao().countCatalogItems(catalogId, systemCatalogId);
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
				String name = item.getFieldValue(ICatalogField.F_NAME);
				if (name == null || "".equals(name.trim())) {
					throw new RuntimeException("CatalogItem's name unset");
				}
				
				//branch
				if (IDetailManager.SYSTEM_BRANCH_ID.equals(branchId)) {
					String branchName = item.getBranchName();
					/*if (branchName == null) {
						branchName = "unset";
						item.setBranchName(branchName);
					}*/
					String unmanagedBranchId = SmartpadCommon.md5(systemCatalogId + SmartpadCommon.standarizeIdentity(branchName));
					Operation unmanageBranch = new OperationDao().loadBranch(unmanagedBranchId);
					if (unmanageBranch == null) {
						unmanageBranch = PartnerManager.instance.createUnmanagedBranch(unmanagedBranchId, systemCatalogId, branchName);
					}
					item.setBranchId(unmanagedBranchId);
					item.setCatalogId(unmanagedBranchId);
				}
				
				String newId = SmartpadCommon.md5(branchId + catalogId + name);
				Catalog syscat = (Catalog) PartnerManager.instance.getSystemCatalog(systemCatalogId);
				while (syscat != null) {
					new CatalogItemDao().insert(newId, syscat.getCatalogSpec(), item);
					syscat = (Catalog) PartnerManager.instance.getSystemCatalog(syscat.getParentCatalogId());
				}
				item.setId(newId);
			}
			
			@Override
			public void update(IUser authorizedUser, ICatalogItem member) throws SQLException {
				CatalogItem item = (CatalogItem) member;
				Catalog syscat = (Catalog) PartnerManager.instance.getSystemCatalog(systemCatalogId);
				while (syscat != null) {
					new CatalogItemDao().update(item.getId(), syscat.getCatalogSpec(), item);
					syscat = (Catalog) PartnerManager.instance.getSystemCatalog(syscat.getParentCatalogId());
				}
			}
			
			@Override
			public void delete(IUser authorizedUser, ICatalogItem member) throws SQLException {
				CatalogItem item = (CatalogItem) member;
				Catalog syscat = (Catalog) PartnerManager.instance.getSystemCatalog(systemCatalogId);
				while (syscat != null) {
					String specId = PartnerManager.instance.getCatalogSpec(systemCatalogId).getSpecId();
					new CatalogItemDao().delete(item.getId(), specId);
					syscat = (Catalog) PartnerManager.instance.getSystemCatalog(syscat.getParentCatalogId());
				}
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
				return StringArrayUtils.compare(o1, o2, ICatalogField.F_NAME);
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
		createPagingLists();
	}
	
	public String getParentCatalogId() {
		return this.parentCatalogId;
	}

	@Override
	public IName getDesc() {
		return this.name;
	}
	
	@Override
	public String getName() {
		return this.name.getName();
	}
	
	@Override
	public void setName(String s) {
		this.name.setName(s);
		createPagingLists();
	}

	@Override
	public IRecordInfo getRecordInfo() {
		return recordInfo;
	}
	
	public void populateSpec(JsonObject json) {
		catalogSpec.populate(json);
	}

	@Override
	public ICatalogSpec getCatalogSpec() {
		if (catalogSpec.getReferTo() == null) {
			return catalogSpec;
		}
		
		return PartnerManager.instance.getCatalogSpec(catalogSpec.getReferTo());
	}
	
	public CatalogSpec getCatalogSpecUnresoved() {
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

	@Override
	public JsonObject generateFeedJson(int layoutOptions, String layoutSyscat) {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_ID, this.catalogId);
		json.addProperty(FIELD_TYPE, this.systemCatalogId != null ? TYPENAME_CAT : TYPENAME_SYSCAT);
		json.addProperty(FIELD_NAME, this.name.getName());
		
		if ((LAYOPT_WITHPARENT & layoutOptions) == LAYOPT_WITHPARENT) {
			json.addProperty(FIELD_UP_ID, this.parentCatalogId);
			json.addProperty(FIELD_UP_NAME, this.parentCatName);
		}
		if ((LAYOPT_WITHSEGMENTS & layoutOptions) == LAYOPT_WITHSEGMENTS) {
			CatalogSpec spec = (CatalogSpec) getCatalogSpec();
			LinkedList<CatalogField> groupingFields = spec.getGroupingFields();
			if (groupingFields != null) {
				JsonArray fieldArray = new JsonArray();
				for (CatalogField f : groupingFields) {
					System.out.println("Grouping: " + f.getAttributeObject(CatalogField.ATT_GROUPING));
					JsonArray ja = (JsonArray) f.getAttributeObject(CatalogField.ATT_GROUPING);
					if (ja == null || ja.isJsonNull()) {
						continue;
					}
					JsonObject fieldJson = new JsonObject();
					fieldJson.addProperty(CatalogField.ATT_GROUPING_FIELD, f.getId());
					JsonArray segmentArray = new JsonArray();
					for (int i = 0; i < ja.size(); i++) {
						JsonObject segmentJson = new JsonObject();
						JsonObject o = ja.get(i).getAsJsonObject();
						segmentJson.addProperty(CatalogField.ATT_GROUPING_VALUEID, o.get(CatalogField.ATT_GROUPING_VALUEID).getAsString());
						segmentJson.addProperty(CatalogField.ATT_GROUPING_VALUE, o.get(CatalogField.ATT_GROUPING_VALUE).getAsString());
						segmentJson.addProperty(CatalogField.ATT_GROUPING_FIELD, f.getId());
						segmentArray.add(segmentJson);
					}
					fieldJson.add("values", segmentArray);
					fieldArray.add(fieldJson);
				}
				json.add("segments", fieldArray);
			}
			json.addProperty(FIELD_BRANCHID, this.branchId);
			json.addProperty(FIELD_BRANCHNAME, this.branchName);
		}
		if (this.systemCatalogId != null && (LAYOPT_WITHBRANCH & layoutOptions) == LAYOPT_WITHBRANCH) {
			json.addProperty(FIELD_BRANCHID, this.branchId);
			json.addProperty(FIELD_BRANCHNAME, this.branchName);
		}
		if (this.systemCatalogId != null && (LAYOPT_WITHSYSCAT & layoutOptions) == LAYOPT_WITHSYSCAT && (layoutSyscat == null || !this.systemCatalogId.equals(layoutSyscat))) {
			json.addProperty(FIELD_SYSCATID, systemCatalogId);
			json.addProperty(FIELD_SYSCATNAME, PartnerManager.instance.getSystemCatalog(systemCatalogId).getName());
		}
		return json;
	}
	
	public void setBranchName(String bn) {
		this.branchName = bn;
		createPagingLists();
	}
	
	public String getBranchName() {
		return this.branchName;
	}
	
	public void setParentCatName(String n) {
		this.parentCatName = n;
		createPagingLists();
	}
	
	public String getParentCatName() {
		return this.parentCatName;
	}
}
