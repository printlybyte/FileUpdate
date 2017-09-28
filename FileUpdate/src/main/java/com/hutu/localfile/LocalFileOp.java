package com.hutu.localfile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hutu.localfile.manager.BXFile;
import com.hutu.localfile.manager.BXFileManager;
import com.hutu.localfile.manager.TbViewManager;
import com.hutu.localfileupdate.R;

/**
 * home2
 * 
 * @author hutuxiansheng
 * 
 */
public class LocalFileOp extends Fragment {
	private Context mContext;
	private TbViewManager mTbViewManager;  //又是一个类
	private String TAG = "LocalFileOp";
	public static View mLocalView = null;
	private int state = 0;
	
	public LocalFileOp(Context mContext) {
		this.mContext = mContext;
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.localefile_browser, container, false);
		mTbViewManager = new TbViewManager(mContext, BXFile.LOCAL_MODE);
		mLocalView = v;
		v.setTag(LocalFileOp.class);   //1，使用getTag区分多个点击事件。2，表示给View添加一个格外的数据，
		initView(v);
		state = 1;
		return v;
		
	}
	
	public static ListView getUpdatingView() {
		if (null != mLocalView) {
			return (ListView)mLocalView.findViewById(R.id.updatingView);
		}
		
		return null;
	}

	private void initView(View v) {
		
		mTbViewManager.InitTopViewList(v);
	}
	
	 @Override  
     public void onHiddenChanged(boolean hidden) {
		 Log.d(TAG, "onHiddenChanged is start " + hidden);
		 	if ((hidden == true) && (state == 1)) {			
		 		
		 	} else if ((state == 1) && (hidden == false)){
		 			mTbViewManager.setLocalData();
		 	}
		 	BXFileManager.getInstance().clear();
		 	mTbViewManager.updateViewTitleInfo( BXFile.LOCAL_MODE, mLocalView, true);
             super.onHiddenChanged(hidden);  
     } 

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_BACK) {
					mTbViewManager.onBackPressed();
					return true;
				}
				return false;
			}
		});
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mTbViewManager.clear();
		super.onDestroy();
	}

}
