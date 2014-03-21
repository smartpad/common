package com.jinnova.smartpad;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import com.jinnova.smartpad.partner.IUser;

public class CachedPagingList<T, E> implements IPagingList<T, E> {
	
	private int totalCount = -1;
	
	private int pageCount = -1;
	
	private int pageSize = 100;
	
	private E sortField;
	
	private boolean ascending;

	/**
	 * maximum 3 pages, first two are never retire, last page does retire
	 */
	private final LinkedList<CachedPage<T>> pages = new LinkedList<>();

	//must be zero-lenght
	private final T[] array;
	
	private final PageMemberMate<T, E> memberMate;
	
	private final Comparator<CachedPage<T>> pageComparator = new Comparator<CachedPage<T>>() {

		@Override
		public int compare(CachedPage<T> o1, CachedPage<T> o2) {
			return o1.getPageNumber() - o1.getPageNumber();
		}
	};
	
	private final Comparator<T> memberComparator;
	
	public CachedPagingList(PageMemberMate<T, E> memberMate, Comparator<T> memberComparator, T[] array) {
		this.memberMate = memberMate;
		this.memberComparator = memberComparator;
		this.array = array;
		this.sortField = memberMate.getDefaultSort();
		this.ascending = memberMate.isDefaultSortAscending();
	}
	
	@Override
	public void setPageSize(int pageSize) {
		this.pageCount = -1;
		this.pageSize = pageSize;
		this.pages.clear();
	}

	@Override
	public void setSortField(E e) {
		this.pageCount = -1;
		this.pages.clear();
	}

	@Override
	public void setSortDirection(boolean ascending) {
		this.pageCount = -1;
		this.pages.clear();
	}
	
	@Override
	public T newMemberInstance() {
		return memberMate.newMemberInstance();
	}
	
	@Override
	public CachedPage<T> loadPage(int pageNumber) throws SQLException {
		
		if (pageSize < 0) {
			//throw new RuntimeException("Negative pageSize");
			return null;
		}
		if (pageNumber < 0) {
			return null;
		}
		
		if (totalCount < 0) {
			totalCount = memberMate.count();
			pageCount = totalCount / pageSize;
			if (totalCount > 0 && totalCount % pageSize != 0) {
				pageCount++;
			}
		}
		
		if (pageNumber > pageCount) {
			return null;
		}
		
		for (CachedPage<T> onePage : pages) {
			if (onePage.getPageNumber() == pageNumber) {
				return onePage;
			}
		}
		
		int offset = (pageNumber - 1) * pageSize;
		LinkedList<T> members = memberMate.load(offset, pageSize, sortField, ascending);
		CachedPage<T> newPage = new CachedPage<>(totalCount, pageCount, pageNumber, offset, members, array);
		pages.add(newPage);
		Collections.sort(pages, pageComparator);
		if (pages.size() > 3) {
			pages.removeLast();
		}
		return newPage;
	}

	@Override
	public void put(IUser authorizedUser, T newMember) throws SQLException {
		if (!authorizedUser.isPrimary()) {
			throw new RuntimeException("Unauthorzied user");
		}
		
		if (memberMate.isPersisted(newMember)) {
			memberMate.update(newMember);
			return;
		}
		
		//add new
		memberMate.insert(newMember);
		boolean putInPage = false;
		Iterator<CachedPage<T>> it = pages.iterator();
		while (it.hasNext()) {
			CachedPage<T> page = it.next();
			if (putInPage) {
				it.remove();
				continue;
			}
			if (page.put(newMember, memberComparator)) {
				putInPage = true;
			}
		}
	}

	@Override
	public void delete(IUser authorizedUser, T newMember) throws SQLException {
		if (!authorizedUser.isPrimary()) {
			throw new RuntimeException("Unauthorzied user");
		}
		memberMate.delete(newMember);
		boolean removedFromPage = false;
		Iterator<CachedPage<T>> it = pages.iterator();
		while (it.hasNext()) {
			CachedPage<T> page = it.next();
			if (removedFromPage) {
				it.remove();
				continue;
			}
			if (page.isInPage(newMember, memberComparator)) {
				it.remove();
				removedFromPage = true;
			}
		}
		
	}
}
