package com.jinnova.smartpad;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class CachedPage<T> implements IPage<T> {
	
	private final int totalCount;
	
	private final int pageCount;
	
	private final int pageNumber;
	
	private final int offset;
	
	private final int pageSize;

	private final LinkedList<T> members;
	
	//must be zero-lenght
	private final T[] array;

	public CachedPage(int totalCount, int pageCount, int pageNumber, 
			int offset, int pageSize, LinkedList<T> members, T[] array) {
		this.totalCount = totalCount;
		this.pageCount = pageCount;
		this.pageNumber = pageNumber;
		this.offset = offset;
		this.pageSize = pageSize;
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
	public T[] getPageEntries() {
		return members.toArray(array);
	}
	
	public void getPageEntries(List<T> list) {
		list.addAll(members);
	}
	
	boolean isInPage(T newMember, Comparator<T> comparator) {

		return comparator.compare(members.getFirst(), newMember) <= 0 &&
				comparator.compare(members.getLast(), newMember) >= 0;
	}
	
	boolean put(T newMember, Comparator<T> comparator) {
		if (!members.isEmpty() && !isInPage(newMember, comparator)) {
			return false;
		}
		members.add(newMember);
		Collections.sort(members, comparator);
		if (members.size() > pageSize) {
			members.removeLast();
		}
		return true;
	}

}
