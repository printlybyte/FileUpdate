/**   
* @Title: Debug.java 
* @Package com.hutu.localfile.util 
* @Description: TODO
* @author Long Li  
* @date 2015-5-9 上午11:44:14 
* @version V1.0   
*/
package com.hutu.localfile.util;

import android.util.Log;

/** 
 * TODO<请描述这个类是干什么的> 
 * @author  Long Li
 * @data:  2015-5-9 上午11:44:14 
 * @version:  V1.0 
 */
public class Debug {
	
	private static boolean Mode = true;	
	
	public static void debuger(String data) {
		if (!Mode)
			return;
		
		Log.d("Debug", data);
	}

}
