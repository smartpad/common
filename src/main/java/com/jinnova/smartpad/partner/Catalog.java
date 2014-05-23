package com.jinnova.smartpad.partner;

import static com.jinnova.smartpad.partner.IDetailManager.*;
import static com.jinnova.smartpad.LinkSupport.*;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.jinnova.smartpad.CachedPage;
import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.Feed;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.JsonSupport;
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
	
	private String name;
	
	private final Name desc;
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private CachedPagingList<ICatalog, ICatalogSort> subCatalogPagingList;
	
	private CachedPagingList<ICatalogItem, ICatalogItemSort> catalogItemPagingList;
	
	private boolean createCatItemClusterTable = false;
	
	//				<fieldId, <segmentValue, segmentId>>
	private HashMap<String, HashMap<String, String>> segments;
	
	public Catalog(String branchId, String storeId, String catalogId, String parentCatalogId, String systemCatalogId) {
		/*if (storeId == null) {
			throw new NullPointerException();
		}*/
		this.branchId = branchId;
		this.storeId = storeId;
		this.catalogId = catalogId;
		this.parentCatalogId = parentCatalogId;
		desc = new Name(TYPENAME_CAT, null, this.catalogId);
		
		if (systemCatalogId == null) {
			this.catalogSpec = new CatalogSpec();
		} else {
			this.catalogSpec = null;
			this.systemCatalogId = systemCatalogId;
		}
		createPagingLists();
	}
	
	public void createPagingLists() {
		//String syscatId = systemCatalogId;
		subCatalogPagingList = createSubCatalogPagingList(branchId, storeId, catalogId, systemCatalogId, branchName, name, gps, createCatItemClusterTable);
		
		//this is for syscat to have system items
		String syscatId = systemCatalogId != null ? systemCatalogId : catalogSpec.getSpecId();
		catalogItemPagingList = createCatalogItemPagingList(branchId, storeId, catalogId, syscatId, parentCatalogId, branchName, name, gps);
	}
	
	public void setCreateCatItemClusterTable() {
		this.createCatItemClusterTable = true;
		//String syscatId = systemCatalogId != null ? systemCatalogId : catalogSpec.getSpecId();
		//String syscatId = systemCatalogId;
		subCatalogPagingList = createSubCatalogPagingList(branchId, storeId, catalogId, systemCatalogId, branchName, name, gps, createCatItemClusterTable);
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
				if (subCat.name == null || "".equals(subCat.name.trim())) {
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
			final String parentCatId, final String branchName, final String catName, final GPSInfo gps) {
		PageEntrySupport<ICatalogItem, ICatalogItemSort> catalogItemSupport = new PageEntrySupport<ICatalogItem, ICatalogItemSort>() {
			

			@Override
			public ICatalogItem newEntryInstance(IUser authorizedUser) {
				CatalogItem ci = new CatalogItem(branchId, storeId, catalogId, systemCatalogId, parentCatId, null);
				CatalogSpec spec = (CatalogSpec) PartnerManager.instance.getCatalogSpec(systemCatalogId);
				if (spec.isManaged()) {
					ci.setBranchName(branchName);
				}
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
				//CatalogSpec spec = (CatalogSpec) PartnerManager.instance.getCatalogSpec(systemCatalogId);
				String itemBranchName = item.getBranchName();
				//if (IDetailManager.SYSTEM_BRANCH_ID.equals(branchId)) {
				if (itemBranchName == null) {
					throw new RuntimeException("missing branch name");
				}
				String itemBranchId = new OperationDao().loadBranchIdByName(systemCatalogId, itemBranchName);
				if (itemBranchId == null) {
					//String unmanagedBranchId = SmartpadCommon.md5(systemCatalogId + SmartpadCommon.standarizeIdentity(itemBranchName));
					String unmanagedBranchId = OperationDao.generateNameDigest(systemCatalogId, itemBranchName);
					PartnerManager.instance.createUnmanagedBranch(unmanagedBranchId, systemCatalogId, itemBranchName);
					/*Operation unmanageBranch = new OperationDao().loadBranch(unmanagedBranchId);
					if (unmanageBranch == null) {
						unmanageBranch = PartnerManager.instance.createUnmanagedBranch(unmanagedBranchId, systemCatalogId, itemBranchName);
					}*/
					itemBranchId = unmanagedBranchId;
				}
				
				if (!itemBranchId.equals(item.getBranchId())) {
					item.setBranchId(itemBranchId);
					item.setStoreId(itemBranchId);
					item.setCatalogId(itemBranchId);
				}
				
				String newId = SmartpadCommon.md5(branchId + catalogId + name);
				Catalog syscat = (Catalog) PartnerManager.instance.getSystemCatalog(systemCatalogId);
				if (syscat == null) {
					throw new RuntimeException("Null systemCatalogId");
				}
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
		return this.desc;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String s) {
		this.name = s;
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
	
	public String getSegmentJson() {
		if (segments == null) {
			return null;
		}
		
		HashMap<String, HashMap<String, String>> segmentCopy = new HashMap<String, HashMap<String,String>>();
		for (Entry<String, HashMap<String, String>> entry : segments.entrySet()) {
			if (entry.getValue().size() < 2) {
				continue;
			}
			segmentCopy.put(entry.getKey(), entry.getValue());
		}
		return JsonSupport.toJson(segmentCopy).toString();
	}
	
	public void populateSegments(JsonObject json) {
		this.segments = JsonSupport.toHashmap(json);
	}
	
	public void addSegment(String fieldId, String segmentId, String segmentValue) {
		
		if (segments == null) {
			segments = new HashMap<>();
		}
		
		//segments: <fieldId, <segmentValue, segmentId>>
		HashMap<String, String> oneSegment = segments.get(fieldId);
		if (oneSegment == null) {
			oneSegment = new HashMap<>();
			segments.put(fieldId, oneSegment);
		}
		
		oneSegment.put(segmentId, segmentValue);
	}

	@Override
	public JsonObject generateFeedJson(int layoutOptions, HashMap<String, Object> layoutParams) {
		if ((LAYOPT_PRIVATECAT & layoutOptions) == LAYOPT_PRIVATECAT) {
			return generateFeedJsonPrivateCat(layoutOptions, layoutParams);
		} else {
			return generateFeedJsonSysCat(layoutOptions, layoutParams);
		}
	}

	private JsonObject generateFeedJsonPrivateCat(int layoutOptions, HashMap<String, Object> layoutParams) {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_ID, this.catalogId);
		
		boolean syscat = this.systemCatalogId == null;
		String typeName = syscat ? TYPENAME_SYSCAT : TYPENAME_CAT;
		json.addProperty(FIELD_TYPE, typeName);
		json.addProperty(FIELD_TYPENUM, this.systemCatalogId != null ? String.valueOf(TYPE_CAT) : String.valueOf(TYPE_SYSCAT));
		
		String linkPrefix = (String) layoutParams.get(LAYOUT_PARAM_LINKPREFIX);
		String nameAndUp = this.name;
		
		if ((LAYOPT_WITHPARENT & layoutOptions) == LAYOPT_WITHPARENT &&
				!this.branchId.equals(this.parentCatalogId)) {
			//json.addProperty(FIELD_UP_ID, this.parentCatalogId);
			//json.addProperty(FIELD_UP_NAME, this.parentCatName);
			
			String upLink = makeDrillLink(linkPrefix, typeName, this.parentCatalogId, this.parentCatName, null);
			if (upLink != null && !"".equals(upLink)) {
				nameAndUp += " (" + upLink + ")";
			}
		}
		json.addProperty(FIELD_NAME, nameAndUp);
		
		String branchCaption = null;
		if (!syscat && (LAYOPT_WITHBRANCH & layoutOptions) == LAYOPT_WITHBRANCH) {
			branchCaption = makeDrillLink(linkPrefix, TYPENAME_BRANCH, this.branchId, this.branchName, null);
		}
		
		String excludeSyscat = (String) layoutParams.get(LAYOUT_PARAM_SYSCAT_EXCLUDE);
		if (!syscat && (LAYOPT_WITHSYSCAT & layoutOptions) == LAYOPT_WITHSYSCAT && 
				(excludeSyscat == null || !this.systemCatalogId.equals(excludeSyscat))) {
			
			String s = makeDrillLink(linkPrefix, TYPENAME_SYSCAT, systemCatalogId, 
					PartnerManager.instance.getSystemCatalog(systemCatalogId).getName(), null);
			if (branchCaption == null) {
				branchCaption = s;
			} else {
				branchCaption += " (" + s + ")";
			}
		}
		
		if (branchCaption != null) {
			json.addProperty(FIELD_BRANCHNAME, branchCaption);
		}
		return json;
	}

	private JsonObject generateFeedJsonSysCat(int layoutOptions, HashMap<String, Object> layoutParams) {
		JsonObject json = new JsonObject();
		json.addProperty(FIELD_ID, this.catalogId);
		
		boolean syscat = this.systemCatalogId == null;
		String typeName = syscat ? TYPENAME_SYSCAT : TYPENAME_CAT;
		json.addProperty(FIELD_TYPE, typeName);
		json.addProperty(FIELD_TYPENUM, this.systemCatalogId != null ? String.valueOf(TYPE_CAT) : String.valueOf(TYPE_SYSCAT));
		
		String linkPrefix = (String) layoutParams.get(LAYOUT_PARAM_LINKPREFIX);
		String nameAndUp = this.name;
		
		if ((LAYOPT_WITHPARENT & layoutOptions) == LAYOPT_WITHPARENT) {
			json.addProperty(FIELD_UP_ID, this.parentCatalogId);
			json.addProperty(FIELD_UP_NAME, this.parentCatName);
			
			String upLink = makeDrillLink(linkPrefix, typeName, this.parentCatalogId, this.parentCatName, null);
			if (upLink != null && !"".equals(upLink)) {
				nameAndUp += " (" + upLink + ")";
			}
		}
		json.addProperty(FIELD_NAME, nameAndUp);
		
		/*if ((LAYOPT_WITHSEGMENTS_REMOVER & layoutOptions) == LAYOPT_WITHSEGMENTS_REMOVER) {
			JsonArray segmentRemoverArray = buildSegmentRemoverJson(layoutParams);
			if (segmentRemoverArray != null) {
				json.add(FIELD_SEGMENT_REMOVER, segmentRemoverArray);
			}
		}*/
		if ((LAYOPT_WITHSEGMENTS & layoutOptions) == LAYOPT_WITHSEGMENTS) {
			if (segments != null /*&& segments.size() > 1*/) {
				/*JsonArray segmentArray = buildSegmentJson(layoutParams);
				if (segmentArray != null && segmentArray.size() > 0) {
					json.add(FIELD_SEGMENT, segmentArray);
				}*/
				String segmentDisplay = buildSegmentDisplay(layoutParams);
				if (segmentDisplay != null && !segmentDisplay.isEmpty()) {
					json.addProperty(FIELD_SEGMENT, segmentDisplay);
				}
			}
		}
		if (!syscat && (LAYOPT_WITHBRANCH & layoutOptions) == LAYOPT_WITHBRANCH) {
			json.addProperty(FIELD_BRANCHID, this.branchId);
			json.addProperty(FIELD_BRANCHNAME, this.branchName);
		}
		
		String excludeSyscat = (String) layoutParams.get(LAYOUT_PARAM_SYSCAT_EXCLUDE);
		if (!syscat && (LAYOPT_WITHSYSCAT & layoutOptions) == LAYOPT_WITHSYSCAT && 
				(excludeSyscat == null || !this.systemCatalogId.equals(excludeSyscat))) {
			json.addProperty(FIELD_SYSCATID, systemCatalogId);
			json.addProperty(FIELD_SYSCATNAME, PartnerManager.instance.getSystemCatalog(systemCatalogId).getName());
		}
		return json;
	}
	
	/*private JsonArray buildSegmentRemoverJson(HashMap<String, Object> layoutParams) {

		@SuppressWarnings("unchecked")
		List<String> segmentParamList = (List<String>) layoutParams.get(Feed.LAYOUT_PARAM_SEGMENTS);
		if (segmentParamList == null || segmentParamList.isEmpty()) {
			return null;
		}
		
		CatalogSpec spec = (CatalogSpec) getCatalogSpec();
		JsonArray segmentRemoverArray = new JsonArray();
		for (String one : segmentParamList) {
			
			int index = one.indexOf(ICatalogField.SEGMENT_PARAM_SEP);
			if (index >= one.length()) {
				continue;
			}
			String segmentField = one.substring(0, index);
			String segmentValueId = one.substring(index + 1);
			String segmentValue = segments.get(segmentField).get(segmentValueId);
			
			SortedSet<String> segmentParamSet = new TreeSet<>();
			segmentParamSet.addAll(segmentParamList);
			segmentParamSet.remove(one);
			String segmentParams = buildSegmentParams(segmentParamSet);
			
			JsonObject segmentJson = new JsonObject();
			segmentJson.addProperty(FIELD_SEGMENT_FIELDID, segmentField);
			segmentJson.addProperty(FIELD_SEGMENT_FIELDNAME, spec.getField(segmentField).getName());
			segmentJson.addProperty(FIELD_SEGMENT_VALUEID, segmentValueId);
			segmentJson.addProperty(FIELD_SEGMENT_VALUE, segmentValue);
			segmentJson.addProperty(FIELD_SEGMENT_LINK, "/syscat/" + catalogId + "/drill" + segmentParams);
			segmentRemoverArray.add(segmentJson);
		}
		return segmentRemoverArray;
	}*/
	
	private String buildOneSegmentRemover(String linkPrefix, List<String> existingParams, String oneExistingParam) {
		
		int index = oneExistingParam.indexOf(ICatalogField.SEGMENT_PARAM_SEP);
		if (index >= oneExistingParam.length()) {
			return null;
		}
		String segmentField = oneExistingParam.substring(0, index);
		String segmentValueId = oneExistingParam.substring(index + 1);
		String segmentValue = segments.get(segmentField).get(segmentValueId);
		
		SortedSet<String> segmentParamSet = new TreeSet<>();
		segmentParamSet.addAll(existingParams);
		segmentParamSet.remove(oneExistingParam);
		String segmentParams = buildParamSet("segments", segmentParamSet);
		return makeDrillLink(linkPrefix, TYPENAME_SYSCAT, this.catalogId, segmentValue, "?" + segmentParams);
		/*JsonObject segmentJson = new JsonObject();
		segmentJson.addProperty(FIELD_SEGMENT_FIELDID, segmentField);
		segmentJson.addProperty(FIELD_SEGMENT_FIELDNAME, spec.getField(segmentField).getName());
		segmentJson.addProperty(FIELD_SEGMENT_VALUEID, segmentValueId);
		segmentJson.addProperty(FIELD_SEGMENT_VALUE, segmentValue);
		segmentJson.addProperty(FIELD_SEGMENT_LINK, "/syscat/" + catalogId + "/drill" + segmentParams);
		segmentRemoverArray.add(segmentJson);*/
	}
	
	private String buildSegmentDisplay(HashMap<String, Object> layoutParams) {
		
		@SuppressWarnings("unchecked")
		List<String> existingSegmentParamList = (List<String>) layoutParams.get(Feed.LAYOUT_PARAM_SEGMENTS);
		CatalogSpec spec = (CatalogSpec) getCatalogSpec();
		String linkPrefix = (String) layoutParams.get(Feed.LAYOUT_PARAM_LINKPREFIX);
		StringBuffer buffer = new StringBuffer();
		for (Entry<String, HashMap<String, String>> entry : segments.entrySet()) {
			
			String segmentField = entry.getKey();
			if (spec.isSegmentHidden(segmentField)) {
				continue;
			}
			
			String segmentFieldName = spec.getField(segmentField).getName();
			buffer.append("<div>" + segmentFieldName + ": ");
			boolean first = true;
			for (Entry<String, String> segmentEntry : entry.getValue().entrySet()) {
				
				String segmentValueId = segmentEntry.getKey();
				String oneSegmentParam = segmentField + ":" + segmentValueId;
				
				if (!first) {
					buffer.append(" | ");
				}
				first = false;
				
				if (existingSegmentParamList.contains(oneSegmentParam)) {
					String removalLink = buildOneSegmentRemover(linkPrefix, existingSegmentParamList, oneSegmentParam);
					if (removalLink != null) {
						buffer.append("<b>" + removalLink + "</b>");
					}
					continue;
				}
				
				SortedSet<String> segmentParamSet = new TreeSet<>();
				segmentParamSet.addAll(existingSegmentParamList);
				segmentParamSet.add(oneSegmentParam);
				buffer.append(makeDrillLink(linkPrefix, TYPENAME_SYSCAT, catalogId, 
						segmentEntry.getValue(), "?" + buildParamSet("segments", segmentParamSet)));
			}
			buffer.append("</div");
		}
		return buffer.toString();
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
