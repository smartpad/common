package com.jinnova.smartpad.partner;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;

import com.jinnova.smartpad.CachedPagingList;
import com.jinnova.smartpad.IName;
import com.jinnova.smartpad.IPagingList;
import com.jinnova.smartpad.Name;
import com.jinnova.smartpad.PageMemberMate;
import com.jinnova.smartpad.RecordInfo;
import com.jinnova.smartpad.db.CatalogDao;
import com.jinnova.smartpad.db.CatalogItemDao;

public class Catalog implements ICatalog {
	
	private final String branchId;
	
	private String catalogId;
	
	private String parentCatalogId;
	
	private String systemParentId;
	
	private final Name name = new Name();

	//private final LinkedList<ICatalog> subCatalogs = new LinkedList<>();

	//private final LinkedList<ICatalogItem> catItems = new LinkedList<>();
	
	private final RecordInfo recordInfo = new RecordInfo();
	
	private final CachedPagingList<ICatalog, ICatalogSort> subCatalogPagingList;
	
	private final CachedPagingList<ICatalogItem, ICatalogItemSort> catalogItemPagingList;
	
	public Catalog(String branchId, String catalogId, String parentCatalogId) {
		this.branchId = branchId;
		this.catalogId = catalogId;
		this.parentCatalogId = parentCatalogId;
		
		PageMemberMate<ICatalog, ICatalogSort> memberMate = new PageMemberMate<ICatalog, ICatalogSort>() {
			
			@Override
			public ICatalog newMemberInstance(IUser authorizedUser) {
				return new Catalog(Catalog.this.branchId, null, Catalog.this.catalogId);
			}
			
			@Override
			public boolean isPersisted(ICatalog member) {
				return ((Catalog) member).catalogId != null;
			}
			
			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new CatalogDao().countSubCatalogs(Catalog.this.catalogId);
			}
			
			@Override
			public LinkedList<ICatalog> load(IUser authorizedUser, int offset,
					int pageSize, ICatalogSort sortField, boolean ascending) throws SQLException {
				
				return new CatalogDao().loadSubCatalogs(Catalog.this.catalogId, offset, pageSize, sortField, ascending);
			}
			
			@Override
			public void insert(IUser authorizedUser, ICatalog newMember) throws SQLException {
				Catalog subCat = (Catalog) newMember;
				if (subCat.name.getName() == null || "".equals(subCat.name.getName().trim())) {
					throw new RuntimeException("Catalog's name unset");
				}
				String newId = SmartpadCommon.md5(subCat.branchId + subCat.name.getName());
				new CatalogDao().insert(subCat.branchId, newId, subCat.parentCatalogId, subCat);
				subCat.catalogId = newId;
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
		Comparator<ICatalog>[] comparators = new Comparator[ICatalogSort.values().length];
		comparators[ICatalogSort.createDate.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o1.getRecordInfo().getCreateDate());
			}
		};
		comparators[ICatalogSort.updateDate.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		comparators[ICatalogSort.createBy.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getCreateBy().compareTo(o2.getRecordInfo().getCreateBy());
			}
		};
		comparators[ICatalogSort.updateBy.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getRecordInfo().getUpdateBy().compareTo(o2.getRecordInfo().getUpdateBy());
			}
		};
		comparators[ICatalogSort.name.ordinal()] = new Comparator<ICatalog>() {
			
			@Override
			public int compare(ICatalog o1, ICatalog o2) {
				return o1.getName().getName().compareTo(o2.getName().getName());
			}
		};
		subCatalogPagingList = new CachedPagingList<ICatalog, ICatalogSort>(
				memberMate, comparators, ICatalogSort.createDate, new Catalog[0]);
		
		@SuppressWarnings("unchecked")
		Comparator<ICatalogItem>[] itemComparators = new Comparator[ICatalogItemSort.values().length];
		itemComparators[ICatalogItemSort.createDate.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getCreateDate().compareTo(o2.getRecordInfo().getCreateDate());
			}
		};
		itemComparators[ICatalogItemSort.updateDate.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getUpdateDate().compareTo(o2.getRecordInfo().getUpdateDate());
			}
		};
		itemComparators[ICatalogItemSort.createBy.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getCreateBy().compareTo(o2.getRecordInfo().getCreateBy());
			}
		};
		itemComparators[ICatalogItemSort.updateBy.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getRecordInfo().getUpdateBy().compareTo(o2.getRecordInfo().getUpdateBy());
			}
		};
		itemComparators[ICatalogItemSort.name.ordinal()] = new Comparator<ICatalogItem>() {
			
			@Override
			public int compare(ICatalogItem o1, ICatalogItem o2) {
				return o1.getName().getName().compareTo(o2.getName().getName());
			}
		};
		PageMemberMate<ICatalogItem, ICatalogItemSort> itemMemberMate = new PageMemberMate<ICatalogItem, ICatalogItemSort>() {
			
			@Override
			public ICatalogItem newMemberInstance(IUser authorizedUser) {
				return new CatalogItem(null);
			}
			
			@Override
			public boolean isPersisted(ICatalogItem member) {
				return ((CatalogItem) member).getItemId() != null;
			}
			
			@Override
			public int count(IUser authorizedUser) throws SQLException {
				return new CatalogItemDao().countCatalogItems(Catalog.this.catalogId);
			}
			
			@Override
			public LinkedList<ICatalogItem> load(IUser authorizedUser, int offset,
					int pageSize, ICatalogItemSort sortField, boolean ascending) throws SQLException {
				
				return new CatalogItemDao().loadCatalogItems(Catalog.this.catalogId, offset, pageSize, sortField, ascending);
			}
			
			@Override
			public void insert(IUser authorizedUser, ICatalogItem newMember) throws SQLException {
				CatalogItem item = (CatalogItem) newMember;
				if (item.getName().getName() == null || "".equals(item.getName().getName().trim())) {
					throw new RuntimeException("CatalogItem's name unset");
				}
				String newId = SmartpadCommon.md5(Catalog.this.branchId + Catalog.this.catalogId + item.getName().getName());
				new CatalogItemDao().insert(Catalog.this.branchId, newId, Catalog.this.catalogId, item);
				item.setItemId(newId);
			}
			
			@Override
			public void update(IUser authorizedUser, ICatalogItem member) throws SQLException {
				CatalogItem item = (CatalogItem) member;
				new CatalogItemDao().update(item.getItemId(), item);
			}
			
			@Override
			public void delete(IUser authorizedUser, ICatalogItem member) throws SQLException {
				CatalogItem item = (CatalogItem) member;
				new CatalogItemDao().delete(item.getItemId());
			}
		};
		catalogItemPagingList = new CachedPagingList<ICatalogItem, ICatalogItemSort>(
				itemMemberMate, itemComparators, ICatalogItemSort.createDate, new CatalogItem[0]);
	}

	@Override
	public ICatalog getSystemParent() {
		return PartnerManager.instance.getSystemCatalog(this.systemParentId);
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
	public IPagingList<ICatalog, ICatalogSort> getSubCatalogPagingList() {
		return subCatalogPagingList;
	}

	@Override
	public IPagingList<ICatalogItem, ICatalogItemSort> getCatalogItemPagingList() {
		return catalogItemPagingList;
	}
}
