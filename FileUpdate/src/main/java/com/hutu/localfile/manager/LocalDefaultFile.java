/**   
 * @Title: LocalVideoFile.java 
 * @Package com.hutu.localfile.manager 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-15 下午5:21:09 
 * @version V1.0   
 */
package com.hutu.localfile.manager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hutu.localfileupdate.R;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author Long Li
 * @data: 2015-5-15 下午5:21:09
 * @version: V1.0
 */
public class LocalDefaultFile extends Fragment {
	private Context mContext;
	private static TbViewManager mTbViewManager;
	private static View mDefaultView = null;
	private String TAG = "LocalDefaultFile";
	View mView = null;
	private int state = 0;

	private static int mAction = 0;

	/**
	 * @param mAction
	 *            the mAction to set
	 */
	public void setmAction(int mAction) {
		this.mAction = mAction;

	}
	public void setOpType() {
		mTbViewManager.setOpType(mAction);
	}

	public LocalDefaultFile(Context mContext) {
		this.mContext = mContext;
		mTbViewManager = new TbViewManager(mContext, mAction);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.localefile_browser, container, false);
		v.setTag(LocalDefaultFile.class);
		mDefaultView = v;
		initView(v);
		setOpType();
		state = 1;
		return v;

	}

	public static ListView getUpdatingView() {
		if (null != mDefaultView) {
			return (ListView) mDefaultView.findViewById(R.id.updatingView);
		}

		return null;
	}

	private void initView(View v) {
		mTbViewManager.InitTopViewList(v);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		Log.d(TAG, "onHiddenChanged is " + hidden);
		if ((hidden == true) && (state == 1)) {
			refreshVideoView();
		}
		super.onHiddenChanged(hidden);
	}

	public static void refreshVideoView() {
		BXFileManager.getInstance().clear();
		mTbViewManager.updateViewTitleInfo(mAction, mDefaultView, true);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onResume");
		
		super.onResume();

	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()motherfucker cocosucker!!! fuckyou
	 * 在上传中的页面退出到后台的时候直接返回到默认界面下，不得已的方法
	 * 就是把页面给finish掉
	 */
	@Override
	public void onStop() {
		getActivity().finish();
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");
		mTbViewManager.clear();
		super.onDestroy();
	}

}
