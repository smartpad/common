package com.jinnova.smartpad;

import java.util.HashMap;

import com.google.gson.JsonObject;

public interface Feed {
	
	static final int LAYOPT_NONE = 0; //0, 1, 2, 4, 8, 16...
	static final int LAYOPT_WITHBRANCH = 1;
	static final int LAYOPT_WITHCAT = 2;
	static final int LAYOPT_WITHSYSCAT = 4;
	static final int LAYOPT_WITHPARENT = 8;
	static final int LAYOPT_WITHSEGMENTS = 16;
	static final int LAYOPT_WITHDETAILS = 32;
	
	static final String LAYOUT_PARAM_SYSCAT_EXCLUDE = "syscatExclude";
	static final String LAYOUT_PARAM_SEGMENTS = "segments";

	JsonObject generateFeedJson(int layoutOptions, HashMap<String, Object> layoutParams);

}
