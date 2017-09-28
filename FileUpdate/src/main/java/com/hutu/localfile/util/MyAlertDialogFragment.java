/**   
 * @Title: MyAlertDialogFragment.java 
 * @Package PrefFragment 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-5-13 下午4:54:48 
 * @version V1.0   
 */
package com.hutu.localfile.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hutu.localfile.manager.BXFile;
import com.hutu.localfile.manager.DataManager;
import com.hutu.localfile.manager.TbViewManager;
import com.hutu.localfileupdate.MainActivity;
import com.hutu.localfileupdate.R;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author Long Li
 * @data: 2015-5-13 下午4:54:48
 * @version: V1.0
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class MyAlertDialogFragment extends DialogFragment {
	private String TAG = "MyAlertDialogFragment";
	static Handler mHandler = null;
	static Context context = null;
	private int opId = 0;
	
	public static MyAlertDialogFragment newInstance(BXFile mBxFile,
			Handler handler, Context mContext) {
		MyAlertDialogFragment frag = new MyAlertDialogFragment();
		Bundle args = new Bundle();
		args.putSerializable("file", mBxFile);
		mHandler = handler;
		frag.setArguments(args);
		context = mContext;
		return frag;
	}

	public Dialog onCreateDialog(Bundle saveInstanceState) {
		// 获取对象实例化时传入的窗口标题。
		final BXFile mBxFile = (BXFile) getArguments().getSerializable("file");
		if (null == mBxFile) {
			return CreateErrorDalog(mBxFile);
		}
		String title = null;
		
		title = mBxFile.getFileName();
		
		if ((Utils.getDefautDirOp() == null) || (Utils.getDefautDirOp().length() == 0)) {
			opId = R.array.file_list_func;			
		} else {
			if (!mBxFile.isDir()) {
				File mFile = new File(mBxFile.getFilePath());
				title = mFile.getParent();
				if (title.equals("/")) {
					return CreateErrorDalog(mBxFile);
				}
			}
			opId = R.array.DefaultOp_func;
		}

		return new AlertDialog.Builder(getActivity()).setTitle(title)
				.setItems(opId, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dealFileDailog(dialog, which, mBxFile);
					}
				}).create();
	}

	public void sendBroadCastToCenter(Context mContext, int type) {
		Intent mIntent = new Intent("com.hutu.startLocalFile");
		mIntent.putExtra("Type", type);
		// 发送广播
		mContext.sendBroadcast(mIntent);
	}
	
	// 文件操作选项
	private void dealFileDailog(DialogInterface dialog, int which,
			final BXFile mFile) {
		String[] items = getResources().getStringArray(opId);
		int BOTTOMVIEW = 5;
		final Message message = Message.obtain();
		message.what = BOTTOMVIEW;
		Utils.setProperty(Utils.OpFile, mFile.getFilePath());
		if (items[which].equals(getString(R.string.deletfile))) {
			Log.d(TAG, "send " + items[which]);
			message.arg1 = 1; // 删除文件后更新当前文件夹下面的数据
			// 弹出确认对话框，然后确认删除
			new AlertDialog.Builder(getActivity())
					.setTitle(getString(R.string.isDeletFile))
					.setPositiveButton(getString(R.string.sure),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									String fPath = new File(mFile.getFilePath())
											.getParentFile().getAbsolutePath();
									message.obj = fPath;

									FileUtils.delete(mFile.getFilePath());
									//删除文件
									//fixme:如果是上传类型，则需要将数据更新
									
									mHandler.sendMessage(message);
								}

							}).setNegativeButton(getString(R.string.cancel), null).show();

		} else if (items[which].equals(getString(R.string.more_op))) {
			setDefaultPath(mFile);
		} else {
			message.arg1 = 2;
			Utils.setProperty(Utils.OpType, items[which]);
			mHandler.sendMessage(message);
		}
	}
	
	private void setDefaultPath(BXFile mFile) {
		String mAutoPath = Utils.DefaultPath[0];
		String mDirString = Utils.getDefautDirOp();
		int index  = 1;
		if (mDirString != null) {
			index = Integer.parseInt(mDirString);
			mAutoPath = Utils.mDirPath.get(index);
		}
		
		Bundle bundle = null;
		String mpath = bundle.getString("file");  
		System.out.println("mpath的值是--------"+mpath); //尝试改变mpath
		System.out.println("Utils---------"+Utils.getPreferences("file"));  //这边没有运行，研究下逻辑，在别的地方调用下这个里的代码
		
//		String mpath = mFile.getFilePath();
		if (!mFile.isDir()) {
			File file = new File(mFile.getFilePath());
			mpath = file.getParent();
		}
		
		if(!FileUtils.getStrCount(mpath, "/")) {
			new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.ErrorDir))
			.setPositiveButton(getString(R.string.sure),null)
			.setNegativeButton(getString(R.string.cancel), null).show();
			return;
		}
		
		Utils.setPreferences(mAutoPath, mpath);			
		ChosePreference.updateEditView(index, mpath);			
		sendBroadCastToCenter(context, 3);
		DataManager mDataManager = DataManager.getInstance(MainActivity.mContext);
		mDataManager.refreshAutoPathData(index - 1);
		Utils.showShortMsg(R.string.successSetDefDir);
	}
	
	private Dialog CreateErrorDalog(BXFile mBxFile) {

		String error = getString(R.string.setDefaultError);
		
		if (mBxFile == null) {
			error = getString(R.string.fileIsNull);
		}
		
		return new AlertDialog.Builder(getActivity())
		.setTitle(error)
		.setPositiveButton(getString(R.string.sure),null)
		.setNegativeButton(getString(R.string.cancel), null).show();

	}
	
}
