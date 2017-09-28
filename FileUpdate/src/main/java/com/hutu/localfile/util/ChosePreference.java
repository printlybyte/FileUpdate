/**   
 * @Title: ChosePreference.java 
 * @Package com.hutu.localfile.util 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-7-21 下午3:49:23 
 * @version V1.0   
 */
package com.hutu.localfile.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.hutu.localfileupdate.R;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author Long Li
 * @data: 2015-7-21 下午3:49:23
 * @version: V1.0
 */
public class ChosePreference extends Preference {

	private CheckBox mBox1, mBox2, mBox3, mBox4, mBox5;
	private Button mButton1, mButton2, mButton3, mButton4, mButton5;
	private EditText mEditText1, mEditText2, mEditText3, mEditText4, mEditText5;

	private static Map<Integer, EditText> mEditText = new HashMap<Integer, EditText>();
	private static Map<Integer, CheckBox> mCheckBox = new HashMap<Integer, CheckBox>();
	private Map<Integer, Integer> mButtonId = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mCheckBoxId = new HashMap<Integer, Integer>();
	
	private Context mContext = null;
	
	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ChosePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		Log.d("xian", "ChosePreference is start");
	}

	public ChosePreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	//是，了解下 editData
	public static void updateEditView(int action, String editData) {
		EditText mEditTextone =  ((Map<Integer, EditText>) mEditText).get(action);
		mEditTextone.setText(editData);
		
/*		String mstate = Utils
				.getPreferences(Utils.AutoMode);		
		if ((mstate != null) &&  (mstate.length() > 0)) {
			CheckBox mBox = mCheckBox.get(action);
			mBox.setChecked(true);
		}*/
	}
	
	//public 

	@Override
	public void onBindView(View view) {
		//Log.d("xian", "onBindView is start");
		mBox1 = (CheckBox)view.findViewById(R.id.checkbox1);
		mBox1.setChecked(false);
		mBox1.setOnClickListener(mOnClickListener);
		mButton1 = (Button) view.findViewById(R.id.button1);
		mEditText1 = (EditText) view.findViewById(R.id.edittext1);
		mButton1.setOnClickListener(mOnClickListener);
		
		mEditText.put(1, mEditText1);
		mCheckBox.put(1, mBox1);
		mCheckBoxId.put(R.id.checkbox1, 0);
		mButtonId.put(R.id.button1, 1);

		mBox2 = (CheckBox)view.findViewById(R.id.checkbox2);
		mBox2.setChecked(false);
		mBox2.setOnClickListener(mOnClickListener);
		mButton2 = (Button) view.findViewById(R.id.button2);
		mEditText2 = (EditText) view.findViewById(R.id.edittext2);
		mButton2.setOnClickListener(mOnClickListener);
		mEditText.put(2, mEditText2);
		mCheckBox.put(2, mBox2);
		mCheckBoxId.put(R.id.checkbox2, 1);
		mButtonId.put(R.id.button2, 2);

		mBox3 = (CheckBox)view.findViewById(R.id.checkbox3);
		mBox3.setChecked(false);
		mBox3.setOnClickListener(mOnClickListener);
		mButton3 = (Button) view.findViewById(R.id.button3);
		mEditText3 = (EditText) view.findViewById(R.id.edittext3);
		mButton3.setOnClickListener(mOnClickListener);
		mEditText.put(3, mEditText3);
		mCheckBox.put(3, mBox3);
		mCheckBoxId.put(R.id.checkbox3, 2);
		mButtonId.put(R.id.button3, 3);

		mBox4 = (CheckBox)view.findViewById(R.id.checkbox4);
		mBox4.setChecked(false);
		mBox4.setOnClickListener(mOnClickListener);
		mButton4 = (Button) view.findViewById(R.id.button4);
		mEditText4 = (EditText) view.findViewById(R.id.edittext4);
		mButton4.setOnClickListener(mOnClickListener);
		mEditText.put(4, mEditText4);
		mCheckBox.put(4, mBox4);
		mCheckBoxId.put(R.id.checkbox4, 3);
		mButtonId.put(R.id.button4, 4);

		mBox5 = (CheckBox)view.findViewById(R.id.checkbox5);
		mBox5.setChecked(false);
		mBox5.setOnClickListener(mOnClickListener);
		mButton5 = (Button) view.findViewById(R.id.button5);
		mEditText5 = (EditText) view.findViewById(R.id.edittext5);
		mButton5.setOnClickListener(mOnClickListener);
		mEditText.put(5, mEditText5);
		mCheckBox.put(5, mBox5);
		mCheckBoxId.put(R.id.checkbox5, 4);
		mButtonId.put(R.id.button5, 5);
		
/*		String mstate = Utils
				.getPreferences(Utils.AutoMode);
		
		if ((mstate != null) &&  (mstate.length() > 0)) {
			checked = true;
		}*/
		
		for (int i = 0; i < Utils.DEFAULT_NUMS; i++) {
			boolean checked = false;
			String  autopath = "";
			String mstate =  Utils.getPreferences(Utils.DefaultPath[i] +Utils.A);
			String mpath =  Utils.getPreferences(Utils.DefaultPath[i]);
			if ((mpath != null) && (mpath.length() > 0)) {
				autopath = mpath;
				if ((mstate != null) && (mstate.length() > 0)) {
					checked = true;
				}
			}
			EditText mText = mEditText.get(i+1);
			mText.setText(autopath);
			CheckBox mBox = mCheckBox.get(i+1);
			mBox.setChecked(checked);
		}		
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		Log.d("xian", "onCreateView is start");
		return LayoutInflater.from(getContext()).inflate(
				R.layout.chosepreference, parent, false);
	}
	
	public void sendBroadCastToCenter(Context mContext, int type) {
		//type是1,2,3,4,5其中中的一个数字
		Intent mIntent = new Intent("com.hutu.startLocalFile");  //向startLocalFile 跳转
		Utils.setDefautDirOp(""+ type);
		mIntent.putExtra("Type", 1); //local path
		// 发送广播
		mContext.sendBroadcast(mIntent);
		System.out.println("点击了浏览的按钮");   //刚才加的
	}
	
	private void UpdateCheckBoxState(int index) {
		
		String state_mode = Utils.DefaultPath[index] + Utils.A;
		String mstate = Utils
				.getPreferences(state_mode);
		boolean checked = false;
		if ((mstate != null) &&  (mstate.length() > 0)) {
			Utils.setPreferences(state_mode, "");
			Utils.showShortMsg(R.string.failedSetAutoUpdate);
		} else {
			Utils.setPreferences(state_mode, "true");
			Utils.showShortMsg(R.string.succesSetAutoUpdate);
			checked = true;
		}
		
		CheckBox mBox = mCheckBox.get(index+1);
		mBox.setChecked(checked);
		
/*		for (int i = 0; i < Utils.DEFAULT_NUMS; i++) {
			String mpath =  Utils.getPreferences(Utils.DefaultPath[i]);
			if ((mpath != null) && (mpath.length() > 0)) {
				CheckBox mBox = mCheckBox.get(i+1);
				
			}
		}	*/
	}
	

	OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			//在这里是那五个按钮，既然五个按钮执行的代码是一样的，那么把textview设置成目录不是在每一个button的代码里执行的，一定还有别的事情，再看看
			
			case R.id.button1:
			case R.id.button2:
			case R.id.button3:
			case R.id.button4:
			case R.id.button5:
				int action = mButtonId.get(v.getId());    //这里带着id发送了一个广播，然后看下一步怎么执行
				//型号action的值是1,2,3,4,5中的一个，这样就可以考虑下一步的事情了好像还有一个参数，我看看怎么办
				
				Log.d("ChosePreference", "action的值是======"+action);
				//追踪一下怎么获取到action的值，这个广播发到哪里了，怎么接受到action的值？
				sendBroadCastToCenter(mContext, action);
				break;
			case R.id.checkbox1:  //这里就是五个复选框
			case R.id.checkbox2:
			case R.id.checkbox3:
			case R.id.checkbox4:
			case R.id.checkbox5:
				int index = mCheckBoxId.get(v.getId());
				String mpath = Utils.getPreferences(Utils.DefaultPath[index]);
				if ((mpath != null) && (mpath.length() > 0)) {
					UpdateCheckBoxState(index);
					//Utils.showShortMsg(R);
				} else {
					CheckBox mCheckBox = (CheckBox) v.findViewById(v.getId());
					mCheckBox.setChecked(false);
					Utils.showSetDefDirDialog();
				}
			break;

			default:
				break;
			}

		}
	};

}
