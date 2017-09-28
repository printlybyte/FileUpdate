/**   
 * @Title: UpdateManager.java 
 * @Package com.hutu.localfile.manager 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-6 上午11:10:54 
 * @version V1.0   
 */
package com.hutu.localfile.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hutu.databases.DbFile;
import com.hutu.net.NetworkManager;
import com.hutu.thread.ITaskThread;
import com.hutu.thread.ThreadPoolManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO<上传管理类:负责管理ftp上传的开始，停止>
 * 
 * @author Long Li
 * @data: 2015-5-6 上午11:10:54
 * @version: V1.0
 */
public class UpdateManager {
	private Handler mHandler;
	private Context mContext;
	private DbFile mDbFile = null;
	// private String TAG = "UpdateManager";
	private DataManager mDataManager;
	static ThreadPoolManager mThreadPoolManager = null;
	private static UpdateManager mUpdateManager = null;

	public static   Map<String, Updater> MyUpdaters = new HashMap<String, Updater>();

	public static UpdateManager getInstance(Handler mHandler, Context mContext) {
		if ((null == mUpdateManager) && (mHandler != null)) {
			mUpdateManager = new UpdateManager(mHandler, mContext);
		}

		return mUpdateManager;
	}

	// 每次初始化view，就初始化一个update
	public UpdateManager(Handler mHandler, Context mContext) {
		this.mHandler = mHandler;
		this.mContext = mContext;
		this.mDbFile = DbFile.getDbInstance(mContext); // 获取数据库
		mDataManager = DataManager.getInstance(mContext);
		mThreadPoolManager = new ThreadPoolManager(4); // 最多四个线程一起
		mThreadPoolManager.setAfterExecute(new afterTask());
		PrefManager.getInstance(mContext, "FileList");

	}

	public  void pause(BXFile file) {
		Updater mUpdater = MyUpdaters.get(file.getFilePath());
		
		
		//加一个判断防止出现空指针
		if (mUpdater!=null) {
			mUpdater.pause();

			MyUpdaters.remove(file.getFilePath());
		}else {


		}

			
		
	}

	// fixme
	public void delet(BXFile file) {
		Updater mUpdater = MyUpdaters.get(file.getFilePath());
		if (null != mUpdater) {
			mUpdater.pause();
			MyUpdaters.remove(file.getFilePath());
		}
	}

	public void start(BXFile file) {
		startUpdate(file);
	}

	private boolean checkNetwork() {
		NetworkManager mNetworkManager = new NetworkManager(mContext);
		if (!mNetworkManager.CheckNetworkPermisson()) {
			Message message = Message.obtain();
			message.what = 9;
			message.obj = mContext;
			mHandler.sendMessage(message);
			return false;
		}

		return true;

	}

	/**
	 * @param infos
	 *            批量上传
	 */
	public void BatchUpdate(List<BXFile> infos) {
		if (checkNetwork()) {
			if (null != infos) {
				for (BXFile mFile : infos) {
					startUpdate(mFile); // 批量上传
				}
			}
		}
	}

	public void startUpdate(BXFile mFile) {
		if (BXFile.UpdatingState.pause == mFile.getUpdatingState()){
			Log.i("QWEQWEA","ZHENGCHANG  return");
		    return;

		}

        if (checkNetwork()) {
			// 添加线程管理
//			MyUpdaters.clear();
			Updater mUpdater = MyUpdaters.get(mFile.getFilePath());
			if (mUpdater == null) {
				mUpdater = new Updater(mContext, mFile, mHandler, mDbFile);
				MyUpdaters.put(mFile.getFilePath(), mUpdater);
				Log.i("QWEQWEA","ZHENGCHANG"+mFile.getFilePath());

			}else {
//				mUpdater.pause();
				Log.i("QWEQWEA","ZHENGCHANG"+mFile.getFilePath());
				Log.i("QWEQWEA","ZHENGCHANG  mUpdater == null"+MyUpdaters.get(mFile.getFilePath()));
			}
			if (mUpdater.isupdating()) {
				Log.i("QWEQWEA","ZHENGCHANG  isadasdasdasdasdasdasdasdasdupdating");
				return;
			}
			
			mThreadPoolManager.execute(mUpdater);
			Log.i("QWEQWEA","第一个 提交的线程");
		}else {
			Log.i("QWEQWEA","ZHENGCHANG  网络异常");
		}
	}
	public void mWait() {
		mThreadPoolManager.pause();
	}
	
	public void resume() {
		mThreadPoolManager.resume();
	}
	
	
	public void Stop(BXFile mFile) {
		Updater mUpdater = MyUpdaters.get(mFile.getFilePath());
		if (mUpdater == null) {
			mUpdater = new Updater(mContext, mFile, mHandler, mDbFile);
			MyUpdaters.put(mFile.getFilePath(), mUpdater);
		}
		if (mUpdater.isupdating()) {
			return;
		}
	
	}
	

	//
	class afterTask implements ITaskThread {

		@Override
		public void run() {
//			// TODO Auto-generated method stub
//			synchronized (mDataManager.getUpdated(BXFile.LOCAL_MODE)) {
//				// 如果是网络出错，则不会再继续循环上传
//				if (!checkNetwork()) {
//					return;
//				}
//				List<BXFile> mFiles = null;
//				// while(0 != mThreadPoolManager.getActiveCount()) {
//				mFiles = mDataManager.getUpdating(BXFile.LOCAL_MODE);
//				Log.d("afterTask", "剩余未上传文件个数 :" + mFiles.size());
//				// }
//				try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				if (mFiles != null) {
//					for (BXFile mBxFile : mFiles) {
//						if (BXFile.UpdatingState.pause == mBxFile.getUpdatingState()) {
//							Log.d("afterTask", "文件名：" + mBxFile.getFileName());
//							continue;
//						}
//
//						Log.d("hutuxiansheng",
//								"新上传文件名：" + mBxFile.getFileName());
//						Updater mUpdater;
//						mUpdater = MyUpdaters.get(mBxFile.getFilePath());
//						if (mUpdater != null) {
//							MyUpdaters.remove(mBxFile.getFilePath());
//							mUpdater = new Updater(mContext, mBxFile, mHandler,
//									mDbFile);
//							MyUpdaters.put(mBxFile.getFilePath(), mUpdater);
//
//							Log.d("qweqwea", "第二工人提交的线程 MyUpdaters===null 不等于" +MyUpdaters.size());
//						}else {
//							Log.d("qweqwea", "第二工人提交的线程 MyUpdaters===null" );
//						}
//
//						mThreadPoolManager.execute(mUpdater);
//						Log.d("qweqwea", "第二工人提交的线程   " );
//					}
//				}else {
//					Log.d("afterTask", "文件名：等于空了   这是怎么回事" );
//				}
//			}
		}

	}

}
