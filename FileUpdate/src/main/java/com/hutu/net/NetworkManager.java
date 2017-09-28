/**   
 * @Title: NetworkManager.java 
 * @Package com.example.androidnetwork 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-20 上午10:30:46 
 * @version V1.0   
 */
package com.hutu.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author Long Li
 * @data: 2015-5-20 上午10:30:46
 * @version: V1.0
 */
public class NetworkManager {
	private Context mContext = null;

	public NetworkManager(Context mContext) {
		this.mContext = mContext;
	}
	
	private int getNetworkType() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo mobNetInfo = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		NetworkInfo wifiNetInfo = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		int netowkrType = 0;
		
		if ((mobNetInfo != null) && (wifiNetInfo != null)) {			
			
			if (wifiNetInfo.getState() == State.CONNECTED) { //1
				netowkrType = 1;
			} else {
				if (mobNetInfo.getState() == State.CONNECTED) { // 2
					netowkrType = 2;
				} else {
					netowkrType = 0; //网络已经断开
				}
			}
		} 
		
		return netowkrType;
		
	}
	
	public boolean CheckNetworkPermisson() {
		SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean setWifi = prefs.getBoolean("wifi_network", true);
		boolean setGPS = prefs.getBoolean("gsm_network", false);		
		int mNetType = getNetworkType();
		if ( mNetType != 0) { //网络连接正常
			//判断网络类型，根据配置来判断是否需要上传
			if (mNetType != 1) { //非wifi网络都默认为GPS
				if (!setGPS) {
					Log.d("debug", "用户设置GPS网络下不能上传");
					return false;
				}
			} else { //wifi网络
				if (!setWifi) {
					Log.d("debug", "用户设置wifi情况下不能上传");
					return false;
				}
			}
		} else {
			
			return false;
		}		
		return true;
	}

}
