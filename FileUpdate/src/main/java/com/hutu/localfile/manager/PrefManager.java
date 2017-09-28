/**   
 * @Title: PreferenceManager.java 
 * @Package com.hutu.localfile.manager 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-13 下午5:45:40 
 * @version V1.0   
 */
package com.hutu.localfile.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * TODO<Preference基本操作>
 * 
 * @author Long Li
 * @data: 2015-5-13 下午5:45:40
 * @version: V1.0
 */
public class PrefManager {

	// private Context mContext;
	private SharedPreferences prefs;
	private static PrefManager mPrefManager = null;

	public static PrefManager getInstance(Context mContext, String filename) {
		if (mPrefManager == null) {
			mPrefManager = new PrefManager(mContext, filename);
		}
		return mPrefManager;
	}

	public PrefManager(Context mContext, String filename) {
		// this.mContext = mContext;
		prefs = mContext.getSharedPreferences(filename, mContext.MODE_PRIVATE);
	}

	public void put(String key, String vaule) {
		if ((null != key) && (key.length() != 0)) {
			Editor editor = prefs.edit();
			editor.putString(key, vaule);
			editor.commit();
		}
	}

	public String getString(String key) {
		String mdata = prefs.getString(key, "");
		return mdata;
	}

	public void remove(String key) {
		if ((null != key) && (key.length() != 0)) {
			Editor editor = prefs.edit();
			editor.remove(key);
			editor.commit();
		}
	}
}
