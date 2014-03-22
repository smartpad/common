package com.jinnova.smartpad;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

class CachedPage<T> implements IPage<T> {
	
	private final int totalCount;
	
	private final int pageCount;
	
	private final int pageNumber;
	
	private final int offset;

	private final LinkedList<T> members;
	
	//must be zero-lenght
	private final T[] array;

	public CachedPage(int totalCount, int pageCount, int pageNumber, int offset, LinkedList<T> members, T[] array) {
		this.totalCount = totalCount;
		this.pageCount = pageCount;
		this.pageNumber = pageNumber;
		this.offset = offset;
		this.members = members;
		this.array = array;
	}

	@Override
	public int getTotalCount() {
		return totalCount;
	}

	@Override
	public int getPageCount() {
		return pageCount;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public int getOffset() {
		return offset;
	}
	
	@Override
	public T[] getMembers() {
		return members.toArray(array);
	}
	
	boolean isInPage(T newMember, Comparator<T> comparator) {

		return comparator.compare(members.getFirst(), newMember) <= 0 &&
				comparator.compare(members.getLast(), newMember) >= 0;
	}
	
	boolean put(T newMember, Comparator<T> comparator) {
		if (!isInPage(newMember, comparator)) {
			return false;
		}
		members.add(newMember);
		Collections.sort(members, comparator);
		members.removeLast();
		return true;
	}

}