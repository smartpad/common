package com.jinnova.smartpad;

import java.util.Arrays;
import java.util.LinkedList;

import com.jinnova.smartpad.IName;

public class Name implements IName {
	
	private String name;
	
	private String description;
	
	private final LinkedList<String> images = new LinkedList<String>();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}

	@Override
	public String[] getImages() {
		return images.toArray(new String[images.size()]);
	}

	@Override
	public void setImages(String[] images) {
		this.images.clear();
		this.images.addAll(Arrays.asList(images));
	}

}
