package com.jinnova.smartpad;

import com.google.gson.JsonObject;

public interface Feed {
	
	static final int LAYOPT_NONE = 0; //0, 1, 2, 4, 8, 16...
	static final int LAYOPT_WITHBRANCH = 1;
	static final int LAYOPT_WITHCAT = 2;
	static final int LAYOPT_WITHSYSCAT = 4;

	JsonObject generateFeedJson(int layoutOptions, String unshownSyscat);

}
