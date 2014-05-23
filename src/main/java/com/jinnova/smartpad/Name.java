package com.jinnova.smartpad;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jinnova.smartpad.IName;

public class Name implements IName {
	
	//private String name;
	
	private String description;
	
	private final HashMap<String, int[]> images = new HashMap<>();
	
	private final String typeName;
	
	private final String subTypeName;
	
	private final String targetId;
	
	public Name(String typeName, String subTypeName, String targetId) {
		this.typeName = typeName;
		this.subTypeName = subTypeName;
		this.targetId = targetId;
	}

	/*public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}*/

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public String getImagesJson() {
		JsonObject json = new JsonObject();
		for (Entry<String, int[]> entry : images.entrySet()) {
			JsonArray ja = JsonSupport.toJsonArray(entry.getValue());
			json.add(entry.getKey(), ja);
		}
		return json.toString();
	}
	
	public void populate(JsonObject json) {
		if (json == null) {
			return;
		}
		for (Entry<String, JsonElement> entry : json.entrySet()) {
			String imageId = entry.getKey();
			int[] names = JsonSupport.toArray(json, imageId);
			if (names == null) {
				continue;
			}
			images.put(imageId, names);
		}
	}
	
	@Override
	public void setImage(String imageId, InputStream image) throws IOException {
		//addImage(imageId, Name.IMAGE_NAME_DEFAULT);
		
		int[] imageNumbers = this.images.get(imageId);
		int newImageNumber = -1;
		if (imageNumbers == null) {
			newImageNumber = 0;
			imageNumbers = new int[] {newImageNumber};
		} else {
			
			int[] newArray = new int[imageNumbers.length + 1];
			System.arraycopy(imageNumbers, 0, newArray, 0, imageNumbers.length);
			
			for (int i = 0; i < imageNumbers.length; i++) {
				if (newImageNumber < imageNumbers[i]) {
					newImageNumber = imageNumbers[i];
				}
			}
			
			newImageNumber++;
			newArray[imageNumbers.length] = newImageNumber;
			imageNumbers = newArray;
		}
		
		images.put(imageId, imageNumbers);
		new ImageSupport().queueIn(typeName, null, targetId, imageId + "_" + newImageNumber, image);
	}

	@Override
	public String[] getImages(String imageId, int size) {
		int[] imageNumbers = images.get(imageId);
		if (imageNumbers == null) {
			return null;
		}
		
		String[] imageNames = new String[imageNumbers.length];
		for (int i = 0; i < imageNumbers.length; i++) {
			imageNames[i] = ImageSupport.buildImagePath(typeName, subTypeName, targetId, imageId + "_" + imageNumbers[i], size);
		}
		return imageNames;
	}

	@Override
	public String getImage(String imageId, int size) {
		int[] imageNumbers = images.get(imageId);
		if (imageNumbers == null || imageNumbers.length == 0) {
			return null;
		}
		//return imageId + "_" + imageNumbers[0];
		return ImageSupport.buildImagePath(typeName, subTypeName, targetId, imageId + "_" + imageNumbers[0], size);
	}

}
