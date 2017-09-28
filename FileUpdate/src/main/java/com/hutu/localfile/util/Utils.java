package com.hutu.localfile.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.hutu.localfile.manager.BXFile;
import com.hutu.localfile.manager.PrefManager;
import com.hutu.localfileupdate.MainActivity;
import com.hutu.localfileupdate.R;
import com.hutu.zhang.PublicUtils;
import com.hutu.zhang.RegUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Utils {

    public static String OpFile = "OpFile"; // 存储被操作的文件
    public static String OpType = "OpType"; // 存储操作类型(剪切、复制)

    public static int DEFAULT_NUMS = 5; //默认5个文件夹目
    public static String MOVE_FNAME = "MoveFile";
    public static String AutoMode = "AutoMode";
    public static String A = "A";
    public static String[] DefaultPath = {"Default_Dir1", "Default_Dir2", "Default_Dir3", "Default_Dir4", "Default_Dir5"};
    public static Map<Integer, String> mDirPath = new HashMap<Integer, String>();
    private static Context context;

    public Utils() {
        mDirPath.clear();
        for (int i = 0; i < DefaultPath.length; i++) {
            mDirPath.put(i + 1, DefaultPath[i]);

        }
    }

    public static void removeFileName(String oldFile) {
        PrefManager mPrefManager = PrefManager.getInstance(null, null);
        if (mPrefManager != null) {
            mPrefManager.remove(oldFile);
        }
    }

    public static String getRemoteFileName(String remotePath, String oldFile, String oldPath) {
        String fileName = "";
        PrefManager mPrefManager = PrefManager.getInstance(null, null);
        if (mPrefManager != null) {
            fileName = mPrefManager.getString(oldFile);
            if (fileName.length() != 0) {
                Log.d("xiaoming", "find old file is " + fileName);
                return fileName;
            }
        }
        //注册码
        String registerCode = null;
        registerCode = RegUtil.strreg;
        if (oldPath != null) {
            if (remotePath.equals("/Images/")) {
                if (PublicUtils.PictureIsRotated(oldPath)) {
                    registerCode += "_0_";//0代表需要顺时针旋转90度
                } else {
                    registerCode += "_1_";//1代表照片正常
                }
            } else if (remotePath.equals("/Videos/")) {
                if (PublicUtils.GetVideoOrientation(oldPath).equals("90")) {
                    registerCode += "_0_";//0代表需要顺时针旋转90度
                }else{
                    registerCode += "_1_";//1代表视频是横着拍的，正常显示
                }
            }

        }


//		SharedPreferences startauto = context.getSharedPreferences(
//				"REG", 0);
//		SharedPreferences.Editor editor = startauto.edit();
//		try {
//			editor.putString("OBJREG", AESTool.encrypt("lyx123456ybf",input));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		editor.commit();
        //获取系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Timestamp mTimestamp = new Timestamp(System.currentTimeMillis());
        String mtime = df.format(mTimestamp);

        String randCode = "";
        randCode += (int) (Math.random() * 9 + 1);
        for (int i = 0; i < 5; i++) {
            randCode += (int) (Math.random() * 10);
        }

        fileName = registerCode + mtime + randCode + "." + FileUtils.getFileFormat(oldFile);
        if (mPrefManager != null) {
            mPrefManager.put(oldFile, fileName); //存储数据
        }

        return fileName;
    }

    public static void showShortMsg(int id) {
        Toast.makeText(MainActivity.mContext, MainActivity.mContext.getString(id),
                Toast.LENGTH_SHORT).show();
    }


    public static void showSetDefDirDialog() {
        new AlertDialog.Builder(MainActivity.mContext)
                .setTitle(MainActivity.mContext.getString(R.string.notice))
                .setMessage(MainActivity.mContext.getString(R.string.noticeData))
                .setPositiveButton(MainActivity.mContext.getString(R.string.sure), null)
                .setNegativeButton(MainActivity.mContext.getString(R.string.cancel), null).show();
    }

    /**
     * @param cxt
     * @return 屏幕宽
     */
    public static int getScreenWidth(Activity cxt) {
        WindowManager m = cxt.getWindowManager();
        Display d = m.getDefaultDisplay();
        return d.getWidth();
    }

    public static String getProperty(String key) {
        String mvalue = System.getProperty(key);
        return mvalue;
    }

    public static void setProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public String getString(Context mContext, int pos) {
        return mContext.getString(pos);
    }

    public static void setDefautDirOp(String action) {
        System.setProperty("DefautDir", action);  //设置指定键对值的系统属性
    }

    public static String getDefautDirOp() {
        String mvalue = System.getProperty("DefautDir");
        return mvalue;
    }

    public static void setPreferences(String key, String value) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.mContext);
        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getPreferences(String key) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.mContext);
        return prefs.getString(key, "");
    }

    /*
     * 存储剪切数据
     */
    public static void StoreMoveFiles(List<BXFile> Files) {
        Properties mproperties = new Properties();
        int i = 0;
        for (BXFile mfile : Files) {
            mproperties.setProperty(MOVE_FNAME + i, mfile.getFilePath());
            i++;
        }

        System.setProperties(mproperties);
    }

    /*
     * 获取剪切数据
     */
    public static List<String> getMoveFiles() {
        Properties pProperties = System.getProperties();
        List<String> mpath = new ArrayList<String>();
        int i = 0;
        while (pProperties.get(MOVE_FNAME + i) != null) {
            mpath.add((String) pProperties.get(MOVE_FNAME + i));
            i++;
        }
        return mpath;
    }

    public static void cleanProperties() {
        Properties pProperties = System.getProperties();
        pProperties.clear();
    }

}
