package com.hutu.localfile;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hutu.localfileupdate.R;
import com.hutu.zhang.SettingServerIP;

/**
 * 
 * @author hutu
 * 
 */
public class SettingFra extends PreferenceFragment {

	private Context mContext;
	SharedPreferences prefs = null;
	private String AUTO_PATH = "auto_path";
	private String AUTO_FILE = "auto_file";
	private String speString = "A&A";
	private String TAG = "SettingFra";
	private Preference settingIPreference,changePasswordPreference;
	public static int IPsettingCounter=0;  //记录点击了IP设置的次数
	private boolean hasShowToast = false;  //判断是否显示 想要更改配置请联系管理员的Toast
	long showToast; //记录显示Toast的时间
	boolean[] selected = new boolean[] { false, false, false };

	public SettingFra(Context mContext) {
		this.mContext = mContext;
	}

	public static void sendBroadCastToCenter(Context mContext, int type) {
		Intent mIntent = new Intent("com.hutu.startLocalFile");
		mIntent.putExtra("Type", type);
		// 发送广播
		mContext.sendBroadcast(mIntent);
	}

	private void setOpDialog() {
		Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(getString(R.string.setting_auto_func));
		builder.setItems(R.array.upOp, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which == 0) {
					SetFileDialog(); // 设置文件类型自动上传
				} else {
					// 设定自动上传目录
					sendBroadCastToCenter(mContext, 1);
				}
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), null);
		builder.show();
	}

	private void SetFileDialog() {
		Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(getString(R.string.auto_file));
		// builder.setIcon(R.drawable.basketball);
		DialogInterface.OnMultiChoiceClickListener mutiListener = new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialogInterface, int which,
					boolean isChecked) {
				selected[which] = isChecked;
			}
		};
		builder.setMultiChoiceItems(R.array.hobby, selected, mutiListener);
		DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				String selectedStr = "";
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] == true) {
						selectedStr = selectedStr
								+ speString
								+ getResources().getStringArray(R.array.hobby)[i];
					}
				}
				Log.d("SettingFra", "click name is " + selectedStr);
				Editor editor = prefs.edit();
				editor.putString(AUTO_FILE, selectedStr);
				editor.commit();
			}
		};
		builder.setPositiveButton(getString(R.string.sure), btnListener);
		builder.show();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		String path = prefs.getString(AUTO_PATH, "");
		String mfile = prefs.getString(AUTO_FILE, "");
		if (mfile.length() != 0) {
			initSelectState(mfile);
		}

		if ((path == null) || (path.length() == 0)) {
			path = getString(R.string.noAuto_path);
		}

		addPreferencesFromResource(R.layout.preferences);

		settingIPreference = findPreference("ServerIp"); // 找到server ip设置的条目
		settingIPreference.setOnPreferenceClickListener(settingIPClickListener); // 设置监听
		
		changePasswordPreference=findPreference("changePassword");
		changePasswordPreference.setOnPreferenceClickListener(changePassword);
	}
	
	
	
	//更改密码的监听
	android.preference.Preference.OnPreferenceClickListener changePassword=new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			showPasswordSetDialog();
			return false;
		}
	};
	
	
	/**
	 * 设置密码的弹窗
	 */
	private void showPasswordSetDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(mContext, R.layout.change_pwd, null);
		dialog.setView(view, 0, 0, 0, 0);// 设置边距为0,保证在2.x的版本上运行没问题

		final EditText edPassWord = (EditText) view
				.findViewById(R.id.pwd_et1);
		final EditText edPassWordConfirm = (EditText) view
				.findViewById(R.id.pwd_et2);

		TextView OK = (TextView) view.findViewById(R.id.pwd_tv1);
		TextView Cancle = (TextView) view.findViewById(R.id.pwd_tv2);

		OK.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String password = edPassWord.getText().toString().trim();
				String passwordConfirm = edPassWordConfirm.getText().toString()
						.trim();
				if (!TextUtils.isEmpty(password) && !passwordConfirm.isEmpty()) {
					// 当输入的2个内容相同
					if (password.equals(passwordConfirm)) {
						Toast.makeText(mContext, "设置成功",
								Toast.LENGTH_SHORT).show();

						SharedPreferences savedPasswordPref = mContext.getSharedPreferences(
								"savedPassword", 0);

						SharedPreferences.Editor editor = savedPasswordPref
								.edit();
						editor.putString("savedPassword", password);
						editor.commit();
						// editor.putString("password",
						// MD5Utils.encode(password)).commit();
						dialog.dismiss();

					} else {
						Toast.makeText(mContext, "两次输入密码不一致",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(mContext, "输入内容不能为空",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		Cancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	

	/**
	 * IP设置那里 的监听事件
	 */
	OnPreferenceClickListener settingIPClickListener = new OnPreferenceClickListener() {

		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			
			
			
			IPsettingCounter++;
			if (IPsettingCounter==10) {
				startActivity(new Intent(getActivity(), SettingServerIP.class));
			}else {
				
				if (System.currentTimeMillis() - showToast > 3000) {
					hasShowToast=false;

				}
				
				if (!hasShowToast) {
					Toast.makeText(getActivity(), R.string.pleaseContactAdmin,
							Toast.LENGTH_SHORT).show();

					showToast = System.currentTimeMillis();
					hasShowToast=true;
					
					
				}
				
				
				return true;
			}
			


			return false;
		}
	};

	// 初始化自动上传文件类型选择默认值
	private void initSelectState(String mFile) {
		String[] mvalues = mFile.split(speString, 4);
		for (int i = 0; i < mvalues.length; i++) {
			if (mvalues[i].equals(getString(R.string.bxfile_image))) {
				selected[0] = true;
			} else if (mvalues[i].equals(getString(R.string.bxfile_music))) {
				selected[1] = true;
			} else if (mvalues[i].equals(getString(R.string.bxfile_video))) {
				selected[2] = true;
			}
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		Log.d("SettingFra", " is show " + hidden);
		if (hidden == false) {
			String path = prefs.getString(AUTO_PATH, "");
			Preference mPreference = findPreference(AUTO_PATH);
			if ((path == null) || (path.length() == 0)) {
				path = getString(R.string.noAuto_path);
			}
			mPreference.setTitle(path);
		}
		super.onHiddenChanged(hidden);
	}
}
