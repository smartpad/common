package com.jinnova.smartpad;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IRecordInfoHolder;
import com.jinnova.smartpad.partner.IUser;
import com.jinnova.smartpad.partner.PartnerManager;

public class CachedPagingList<T, E extends Enum<?>> implements IPagingList<T, E> {
	
	private int totalCount = -1;
	
	private int pageCount = -1;
	
	private int pageSize;
	
	private E sortField;
	
	private boolean ascending;
	
	private Comparator<T>[] comparators;

	/**
	 * maximum 3 pages, first two are never retire, last page does retire
	 */
	private final LinkedList<CachedPage<T>> pages = new LinkedList<>();

	//must be zero-lenght
	private final T[] array;
	
	private final PageEntrySupport<T, E> memberMate;
	
	private final Comparator<CachedPage<T>> pageComparator = new Comparator<CachedPage<T>>() {

		@Override
		public int compare(CachedPage<T> o1, CachedPage<T> o2) {
			return o1.getPageNumber() - o1.getPageNumber();
		}
	};
	
	public CachedPagingList(PageEntrySupport<T, E> memberMate, Comparator<T>[] comparators, E defaultSort, T[] array) {
		this(memberMate, comparators, defaultSort, true, 100, array);
	}
	
	public CachedPagingList(PageEntrySupport<T, E> memberMate, Comparator<T>[] comparators, 
			E defaultSort, boolean defaultAscending, int defaultPageSize, T[] array) {
		
		if (defaultSort == null) {
			throw new IllegalArgumentException("Default sort can't be null");
		}
		/*for (int i = 0; i < comparators.length; i++) {
			Comparator<T> c = comparators[i];
			if (c == null) {
				throw new IllegalArgumentException("Null comparator at " + i);
			}
		}*/
		
		this.memberMate = memberMate;
		this.comparators = comparators;
		this.array = array;
		this.sortField = defaultSort;
		this.ascending = defaultAscending;
		this.pageSize = defaultPageSize;
	}
	
	@Override
	public IPagingList<T, E> setPageSize(int pageSize) {
		this.pageCount = -1;
		this.pageSize = pageSize;
		this.pages.clear();
		return this;
	}

	@Override
	public IPagingList<T, E> setSortField(E e) {
		this.totalCount = -1;
		this.pageCount = -1;
		this.pages.clear();
		return this;
	}

	@Override
	public IPagingList<T, E> setSortDirection(boolean ascending) {
		this.totalCount = -1;
		this.pageCount = -1;
		this.pages.clear();
		return this;
	}
	
	@Override
	public T newEntryInstance(IUser authorizedUser) {
		return memberMate.newEntryInstance(authorizedUser);
	}
	
	public CachedPage<T> loadPage(int pageNumber) throws SQLException {
		return loadPage(PartnerManager.instance.systemUser, pageNumber);
	}
	
	@Override
	public CachedPage<T> loadPage(IUser authorizedUser, int pageNumber) throws SQLException {
		
		/*if (pageSize <= 0) {
			//throw new RuntimeException("Negative pageSize");
			//return null;
			return new CachedPage<>(totalCount, pageCount, pageNumber, -1, new LinkedList<T>(), array);
		}*/
		if (pageNumber < 0) {
			//return null;
			return new CachedPage<>(totalCount, pageCount, pageNumber, -1, this.pageSize, new LinkedList<T>(), array);
		}
		
		if (pageSize > 0) {
			if (pageCount < 0 || totalCount < 0) {
				totalCount = memberMate.count(authorizedUser);
				/*if (pageSize > 0) {
					pageCount = totalCount / pageSize;
				} else {
					pageCount = 1;
				}*/
				pageCount = totalCount / pageSize;
				if (pageSize > 0 && totalCount > 0 && totalCount % pageSize != 0) {
					pageCount++;
				}
			}
		} else {
			pageCount = 1;
		}
		
		if (pageNumber > pageCount) {
			return new CachedPage<>(totalCount, pageCount, pageNumber, -1, this.pageSize, new LinkedList<T>(), array);
		}
		
		for (CachedPage<T> onePage : pages) {
			if (onePage.getPageNumber() == pageNumber) {
				return onePage;
			}
		}
		
		int offset = (pageNumber - 1) * pageSize;
		LinkedList<T> members = memberMate.load(authorizedUser, offset, pageSize, sortField, ascending);
		CachedPage<T> newPage = new CachedPage<>(totalCount, pageCount, pageNumber, offset, this.pageSize, members, array);
		pages.add(newPage);
		Collections.sort(pages, pageComparator);
		if (pages.size() > 3) {
			pages.removeLast();
		}
		return newPage;
	}
	
	public CachedPage<T> loadFromOffset(int offset) throws SQLException {
		
		if (offset < 0) {
			//return null;
			return new CachedPage<>(-1, 1, -1, offset, this.pageSize, new LinkedList<T>(), array);
		}
		
		LinkedList<T> members = memberMate.load(PartnerManager.instance.systemUser, offset, pageSize, sortField, ascending);
		return new CachedPage<>(-1, 1, -1, offset, this.pageSize, members, array);
	}

	@Override
	public void put(IUser authorizedUser, T newMember) throws SQLException {
		if (!authorizedUser.isPrimary()) {
			throw new RuntimeException("Unauthorzied user");
		}
		
		if (memberMate.isPersisted(newMember)) {
			if (newMember instanceof IRecordInfoHolder) {
				((RecordInfo) ((IRecordInfoHolder) newMember).getRecordInfo()).setUpdateDate(new Date());
				((RecordInfo) ((IRecordInfoHolder) newMember).getRecordInfo()).setUpdateBy(authorizedUser.getLogin());
			}
			memberMate.update(authorizedUser, newMember);
			return;
		}
		
		//add new
		if (newMember instanceof IRecordInfoHolder) {
			((RecordInfo) ((IRecordInfoHolder) newMember).getRecordInfo()).setCreateDate(new Date());
			((RecordInfo) ((IRecordInfoHolder) newMember).getRecordInfo()).setCreateBy(authorizedUser.getLogin());
		}
		memberMate.insert(authorizedUser, newMember);
		boolean putInPage = false;
		Iterator<CachedPage<T>> it = pages.iterator();
		while (it.hasNext()) {
			CachedPage<T> page = it.next();
			if (putInPage) {
				it.remove();
				continue;
			}
			
			int sortIndex = this.sortField != null ? this.sortField.ordinal() : 0;
			if (page.put(newMember, comparators[sortIndex])) {
				putInPage = true;
			}
		}
	}

	@Override
	public void delete(IUser authorizedUser, T newMember) throws SQLException {
		if (!authorizedUser.isPrimary()) {
			throw new RuntimeException("Unauthorzied user");
		}
		memberMate.delete(authorizedUser, newMember);
		boolean removedFromPage = false;
		Iterator<CachedPage<T>> it = pages.iterator();
		while (it.hasNext()) {
			CachedPage<T> page = it.next();
			if (removedFromPage) {
				it.remove();
				continue;
			}
			
			int sortIndex = this.sortField != null ? this.sortField.ordinal() : 0;
			if (page.isInPage(newMember, comparators[sortIndex])) {
				it.remove();
				removedFromPage = true;
			}
		}
		
	}
}
