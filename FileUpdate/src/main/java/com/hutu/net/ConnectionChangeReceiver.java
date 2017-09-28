/**   
 * @Title: ConnectionChangeReceiver.java 
 * @Package com.example.androidnetwork 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-20 上午9:22:44 
 * @version V1.0   
 */
package com.hutu.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author Long Li
 * @data: 2015-5-20 上午9:22:44
 * @version: V1.0
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo mobNetInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		NetworkInfo wifiNetInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		int netowkrType = 0;

		if ((mobNetInfo != null) && (wifiNetInfo != null)) {			
			
			if (wifiNetInfo.getState() == State.CONNECTED) { //1
				netowkrType = 1;
//				Toast.makeText(context, "1", Toast.LENGTH_SHORT).show();
			} else {
				if (mobNetInfo.getState() == State.CONNECTED) { // 2
					netowkrType = 2;
//					Toast.makeText(context, "2", Toast.LENGTH_SHORT).show();
				} else {
//					Toast.makeText(context, "0", Toast.LENGTH_SHORT).show();
					netowkrType = 0;
				}
			}
			
			sendBroadCastToCenter(context, netowkrType);
		} 
	}
	
	public void  sendBroadCastToCenter(Context mContext, int type) {
		Intent mIntent = new Intent("com.hutu.networkChange");  
        mIntent.putExtra("Type", type);         
        //发送广播  
        mContext.sendBroadcast(mIntent);  
	}

}
