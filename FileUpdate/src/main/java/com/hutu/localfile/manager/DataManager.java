/**
 * @Title: DataManager.java
 * @Package com.hutu.localfile.manager
 * @Description: TODO
 * @author Long Li
 * @date 2015-5-15 下午1:21:24
 * @version V1.0
 */
package com.hutu.localfile.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hutu.databases.DbFile;
import com.hutu.localfile.manager.BXFile.FileState;
import com.hutu.localfile.manager.BXFile.UpdatingState;
import com.hutu.localfile.util.Utils;

public class DataManager {

    private static Map<Integer, Map<String, Map<String, BXFile>>> autoDefFiles = new HashMap<Integer, Map<String, Map<String, BXFile>>>();
    // 上传完的文件

    public static String UnUpdated = "UNUPDATED";
    public static String UpDating = "UPDATING";
    public static String Updated = "UPDATED";

    private String[] DataState = {UnUpdated, UpDating, Updated};

    private static Map<String, Integer> mStateMap = new HashMap<String, Integer>();

    private static DbFile mDbfile = null;

    private static Context mContext;

    private static DataManager mDataManager;

    private boolean RefreshResult = false;

    List<BXFile> mOldupdates = new ArrayList<BXFile>();

    // private boolean RefreshData = true;

    public static DataManager getInstance(Context context) {
        if (null == mDataManager) {
            mContext = context;
            mDbfile = DbFile.getDbInstance(mContext);
            mDataManager = new DataManager();
            mStateMap.put(UnUpdated, 0);
            mStateMap.put(UpDating, 1);
            mStateMap.put(Updated, 2);
        }

        return mDataManager;
    }

    public boolean getRefreshDataResult() {
        return RefreshResult;
    }

    public Map<String, BXFile> getAllUpdating() {
        Map<String, BXFile> mLocalListFiles = new HashMap<String, BXFile>();
        synchronized (autoDefFiles) {
            if (autoDefFiles.get(BXFile.LOCAL_MODE) != null) {
                mLocalListFiles = autoDefFiles.get(BXFile.LOCAL_MODE).get(
                        UpDating);
            }
        }
        return mLocalListFiles;
    }

    public Map<String, BXFile> getAllUpdated() {
        Map<String, BXFile> mLocalListFiles = new HashMap<String, BXFile>();
        if (autoDefFiles.get(BXFile.LOCAL_MODE) != null) {
            mLocalListFiles = autoDefFiles.get(BXFile.LOCAL_MODE).get(Updated);
        }
        return mLocalListFiles;
    }

    public String getFile_Path(String str) {
        int ii = 0;
        int j = 0;
        ii = str.indexOf("/");
        j = str.lastIndexOf("/");
        return str.substring(ii, j);
    }

    public List<BXFile> getTypeDatas(int type, String mode) {
        List<BXFile> mupdates = new ArrayList<BXFile>();
        Map<String, BXFile> munupdates = new HashMap<String, BXFile>();

        synchronized (autoDefFiles) {
            if (autoDefFiles.get(type) != null) {

                munupdates = autoDefFiles.get(type).get(mode);
                if (munupdates != null) {
                    try {
                        synchronized (autoDefFiles) {
                            for (Map.Entry<String, BXFile> entry : munupdates
                                    .entrySet()) {
                                mupdates.add(entry.getValue());

                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }
        return mupdates;
    }

    public List<BXFile> getUnupdate(int type) {
        return getTypeDatas(type, UnUpdated);
    }

    public List<BXFile> getUpdating(int type) {
        return getTypeDatas(type, UpDating);
    }

    public List<BXFile> getUpdated(int type) {
        return getTypeDatas(type, Updated);
    }

    //debug 1
    private boolean checkDataExist(int type, String mode, BXFile mBxFile,
                                   int index) {
        Map<String, BXFile> nListFiles = new HashMap<String, BXFile>();
        Map<String, BXFile> mListFiles = new HashMap<String, BXFile>();

        if ((index == 0) || (index == 1)) {
            if (autoDefFiles.get(type).get(DataState[mStateMap.get(mode)]) != null) {
                mListFiles = autoDefFiles.get(type).get(
                        DataState[mStateMap.get(mode)]);
            }
        }

        if ((index == 2) || (index == 1)) {
            if (autoDefFiles.get(type).get(DataState[mStateMap.get(mode)]) != null) {
                nListFiles = autoDefFiles.get(type).get(
                        DataState[mStateMap.get(mode)]);
            }
        }

        if ((nListFiles.get(mBxFile.getFilePath()) != null)
                || (mListFiles.get(mBxFile.getFilePath()) != null)) {
            return true;
        }

        return false;
    }

    private int GetTypeThroughSp(BXFile mBxFile) {
        int a = -1;
        for (int i = 0; i < 5; i++) {
            String mpath = Utils.getPreferences(Utils.DefaultPath[i]);
            String mBxFilePath = getFile_Path(mBxFile.getFilePath());
            if (mpath != null && mBxFilePath != null) {
                if (mpath.equals(mBxFilePath)) {
                    a = i;
                    return i;
                }
            }
        }
        return a;
    }

    //debug 2
    private void addBxFilesData(int type, BXFile mBxFile, String mode) {
        synchronized (autoDefFiles) {
//			for (int i = 0; i < (BXFile.LOCAL_MODE + 1); i++) {
            type = GetTypeThroughSp(mBxFile);
            Map<String, BXFile> mListFiles = new HashMap<String, BXFile>();
            if (autoDefFiles.get(type) != null) {
                if (autoDefFiles.get(type).get(mode) != null) {
                    mListFiles = autoDefFiles.get(type).get(mode);
                }
            }

            if (autoDefFiles.get(type) != null) {
                int mindex = mStateMap.get(mode);
                if (checkDataExist(type, mode, mBxFile, mindex)) {
//						continue;
                }
            }

            mListFiles.put(mBxFile.getFilePath(), mBxFile);
            if (autoDefFiles.get(type) != null) {
                synchronized (autoDefFiles) {
                    autoDefFiles.get(type).put(mode, mListFiles);
                }

            } else {
                Map<String, Map<String, BXFile>> mTypeFils = new HashMap<String, Map<String, BXFile>>();
                mTypeFils.put(mode, mListFiles);
                synchronized (autoDefFiles) {
                    autoDefFiles.put(type, mTypeFils);
                }
            }
//			}
        }

        updateViewData(true); // 7.7改正重大BUG 不能自动上传
    }

    public void addUpdated(int type, BXFile mBxFile) {
        if (UpdatingState.end == mBxFile.getUpdatingState()) {
            return;
        }

        mBxFile.setFileState(FileState.UPDATED);
        mBxFile.setUpdatingState(UpdatingState.end);
        addBxFilesData(type, mBxFile, Updated);
    }

    public void AddUpdating(int type, BXFile mBxFile) {
        mBxFile.setFileState(FileState.UPDATING);

        addBxFilesData(type, mBxFile, UpDating);

    }

    public void AddUnupdate(int type, BXFile mBxFile) {
        mBxFile.setFileState(FileState.UNUPDATE);

        addBxFilesData(type, mBxFile, UnUpdated);
    }

    private void removeBxFilesData(int type, BXFile mBxFile, String mode) {
        if (mBxFile.getFilePath() == null) {
            return;
        }
        synchronized (autoDefFiles) {
            for (int i = 0; i < (BXFile.LOCAL_MODE + 1); i++) { // 避免设定目录重复或者包含与被包含，每次数据都全部查找

                Map<String, BXFile> mListFiles = new HashMap<String, BXFile>();

                if (autoDefFiles.get(i) != null) {
                    if (autoDefFiles.get(i).get(mode) != null) {
                        mListFiles = autoDefFiles.get(i).get(mode);
                    } else {
                        continue;
                    }
                }

                if (mListFiles.get(mBxFile.getFilePath()) == null)
                    continue;

                mListFiles.remove(mBxFile.getFilePath());
                autoDefFiles.get(i).put(mode, mListFiles);
            }
        }
    }

    public void RomveUpdated(int type, BXFile mBxFile) {
        removeBxFilesData(type, mBxFile, Updated);
    }

    public void RomveUnupdate(int type, BXFile mBxFile) {
        removeBxFilesData(type, mBxFile, UnUpdated);
    }

    // 控制所有上传数据的start和pause状态
    public void setUpdatingState(UpdatingState mState, int type, BXFile mBxFile) {
        mBxFile.setUpdatingState(mState);
        AddUpdating(type, mBxFile);
    }

    public void RomveUpdating(int type, BXFile mBxFile) {
        removeBxFilesData(type, mBxFile, UpDating);
    }

    // 更新数据
    public synchronized static void updateViewData(boolean mode) {
        // fixme
        Map<String, BXFile> mLocalUnupdateListFiles = new HashMap<String, BXFile>();
        Map<String, BXFile> mLocalUpdatingListFiles = new HashMap<String, BXFile>();// 所有上传中的文件
        Map<String, BXFile> mLocalUpdatedListFiles = new HashMap<String, BXFile>();

        for (int i = 0; i < Utils.DEFAULT_NUMS; i++) {
            if (autoDefFiles.get(i) != null) {
                synchronized (autoDefFiles) {
                    Map<String, BXFile> unupdates = autoDefFiles.get(i).get(
                            UnUpdated);
                    if (unupdates != null) {
                        for (Map.Entry<String, BXFile> entry : unupdates
                                .entrySet()) {
                            mLocalUnupdateListFiles.put(entry.getValue()
                                    .getFilePath(), entry.getValue());
                        }
                    }
                }

                synchronized (autoDefFiles) {
                    Map<String, BXFile> updatings = autoDefFiles.get(i).get(
                            UpDating);
                    if (updatings != null) {
                        for (Map.Entry<String, BXFile> entry : updatings
                                .entrySet()) {
                            mLocalUpdatingListFiles.put(entry.getValue()
                                    .getFilePath(), entry.getValue());
                        }
                    }
                }

                synchronized (autoDefFiles) {
                    Map<String, BXFile> updateds = autoDefFiles.get(i).get(
                            Updated);
                    if (updateds != null) {
                        for (Map.Entry<String, BXFile> entry : updateds
                                .entrySet()) {
                            mLocalUpdatedListFiles.put(entry.getValue()
                                    .getFilePath(), entry.getValue());
                        }
                    }
                }
            }
        }

        Map<String, Map<String, BXFile>> mTypeFils = new HashMap<String, Map<String, BXFile>>();
        mTypeFils.put(UnUpdated, mLocalUnupdateListFiles);
        mTypeFils.put(UpDating, mLocalUpdatingListFiles);
        mTypeFils.put(Updated, mLocalUpdatedListFiles);
        synchronized (autoDefFiles) {
            autoDefFiles.put(BXFile.LOCAL_MODE, mTypeFils);
        }

        // 0 -no network
        // 1 - wifi 2-GPS
        // 3 - fresh over
        if (mode) {
            sendBroadCastToCenter(mContext, 3);
        }
    }

    // 通知界面 数据已经更新完毕
    public static void sendBroadCastToCenter(Context mContext, int type) {
        Intent mIntent = new Intent("com.hutu.networkChange");
        mIntent.putExtra("Type", type);
        Log.d("hutuxiansheng", "send b to cent");
        // 发送广播
        mContext.sendBroadcast(mIntent);
    }

    // 检查上传中和上传过的文件中是否存在该文件，在本地显示文件时。
    public boolean checkLocalFileState(BXFile mBxFile) {
        Map<String, BXFile> updatingBxFile = getAllUpdating();
        Map<String, BXFile> updatedBxFile = getAllUpdated();

        if (null != updatingBxFile) {
            if (null != updatingBxFile.get(mBxFile.getFilePath())) {
                return true;
            }
        }

        if (null != updatedBxFile) {
            if (null != updatedBxFile.get(mBxFile.getFilePath())) {
                return true;
            }
        }

        return false;
    }

    /** 添加新的默认目录后，获取目录底下数据
     * @param final int mDataAction 的值是 设置页面 五个默认目录的位数，从0开始；0,1,2,3,4
     */
    public void refreshAutoPathData(final int mDataAction) {
        LFBApplication bxApp = (LFBApplication) ((Activity) mContext)
                .getApplication();
        bxApp.execRunnable(new Runnable() {
            @Override
            public void run() {
                InitSupportData(mDataAction);
                updateViewData(true);
            }
        });
    }

    private static void setLocalDatas(int action,
                                      Map<String, BXFile> mUnupdateListFiles,
                                      Map<String, BXFile> mUpdatingListFiles,
                                      Map<String, BXFile> mUpdatedListFiles, List<BXFile> mBxFiles) {

        Map<String, Map<String, BXFile>> mTypeFils = new HashMap<String, Map<String, BXFile>>();
        mTypeFils.put(UnUpdated, mUnupdateListFiles);
        mTypeFils.put(UpDating, mUpdatingListFiles);
        mTypeFils.put(Updated, mUpdatedListFiles);

        mDbfile.updateDbFile(mBxFiles);
        mBxFiles.clear();

        synchronized (autoDefFiles) {
            autoDefFiles.put(action, mTypeFils);
        }
        // Log.d("hutuxiansheng", "start size is " +
        // autoDefFiles.get(action).get(UnUpdated).size());
    }

	/*
     * 自动更新扫描是否有新文件
	 */

    public boolean ScanSupportData(int action) {


        boolean result = false;

        if (action < 0 || action > 4) {
            return result;
        }

        String mpath = Utils.getPreferences(Utils.DefaultPath[action]);
        if ((mpath != null) && (mpath.length() > 0)) {
            BXFileManager mBxFileManager = BXFileManager.getInstance();
            List<BXFile> mFiles = mBxFileManager.getAutoLocalFiles(mpath);

            // 更新数据库
            List<BXFile> mBxFiles = new ArrayList<BXFile>();
            for (BXFile mFile : mFiles) {
                BXFile dbfile = mDbfile.getFileInfos(mFile.getFileName(),
                        mFile.getFilePath());
                if (dbfile == null) {
                    result |= true;
                    mBxFiles.add(mFile);
                }
            }

            if (mBxFiles.size() != 0) {
                mDbfile.updateDbFile(mBxFiles);
                synchronized (autoDefFiles) {
                    Map<String, Map<String, BXFile>> mTypeFils = new HashMap<String, Map<String, BXFile>>();
                    Map<String, BXFile> mUnupdateListFiles = new HashMap<String, BXFile>();

                    if (autoDefFiles.get(action) != null) {
                        mTypeFils = autoDefFiles.get(action);
                        if (autoDefFiles.get(action).get(UnUpdated) != null) {
                            mUnupdateListFiles = autoDefFiles.get(action).get(
                                    UnUpdated);
                        }
                        // 7.11修改 修复刚开始设置默认路径崩溃的bug
                        // if (autoDefFiles.get(action).get(UnUpdated) != null)
                        // {
                        // mUnupdateListFiles =
                        // autoDefFiles.get(action).get(UnUpdated);
                    }
                    for (BXFile mBxFile : mBxFiles) {
                        mUnupdateListFiles.put(mBxFile.getFilePath(), mBxFile);
                    }
                    mTypeFils.put(UnUpdated, mUnupdateListFiles);
                    autoDefFiles.put(action, mTypeFils);
                }

            }

        }

        return result;
    }

    /*
     * 每次从数据库中查找该文件状态，如果没有文件则添加到数据中
     */
    public static boolean InitSupportData(int action) {
        boolean result = false;
        if (action < 0 || action > 4) {
            return result;
        }
        String mpath = Utils.getPreferences(Utils.DefaultPath[action]);
        System.out.println("new--DataMangaer--InitSupportData下面if语句  (mpath != null) && (mpath.length() > 0) ==" + ((mpath != null) && (mpath.length() > 0)));

        if ((mpath != null) && (mpath.length() > 0)) {
            BXFileManager mBxFileManager = BXFileManager.getInstance();
            List<BXFile> mFiles = mBxFileManager.getAutoLocalFiles(mpath);
            Map<String, BXFile> mUnupdateListFiles = new HashMap<String, BXFile>();
            Map<String, BXFile> mUpdatingListFiles = new HashMap<String, BXFile>();
            Map<String, BXFile> mUpdatedListFiles = new HashMap<String, BXFile>();
            // 更新数据库
            List<BXFile> mBxFiles = new ArrayList<BXFile>();
            int index = 0;
            for (BXFile mFile : mFiles) {
                BXFile dbfile = mDbfile.getFileInfos(mFile.getFileName(),
                        mFile.getFilePath());
                if (dbfile != null) {
                    if (dbfile.getFileState() == FileState.UNUPDATE) {
                        mUnupdateListFiles.put(dbfile.getFilePath(), dbfile);
                    } else if (dbfile.getFileState() == FileState.UPDATING) {
                        mUpdatingListFiles.put(dbfile.getFilePath(), dbfile);
                    } else if (dbfile.getFileState() == FileState.UPDATED) {
                        mUpdatedListFiles.put(dbfile.getFilePath(), dbfile);
                    }
                } else {
                    result |= true;
                    mBxFiles.add(mFile);
                    mUnupdateListFiles.put(mFile.getFilePath(), mFile);
                }
                index++;
                // 数据比较大时，满100就更新
                if ((index % 100) == 0) {
                    setLocalDatas(action, mUnupdateListFiles,
                            mUpdatingListFiles, mUpdatedListFiles, mBxFiles);
                    updateViewData(true);
                }

            }
            setLocalDatas(action, mUnupdateListFiles, mUpdatingListFiles,
                    mUpdatedListFiles, mBxFiles);
        }

        return result;
    }

    // 获取所有支持上传的本地数据
    public void getSupportData() {
        LFBApplication bxApp = (LFBApplication) ((Activity) mContext)
                .getApplication();
        bxApp.execRunnable(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (int i = 0; i < Utils.DEFAULT_NUMS; i++) {
                    InitSupportData(i);
                    updateViewData(true);
                }

                RefreshResult = true;
            }
        });
    }

    /*
     * 批量设置上传状态
     */
    public boolean changeUnupDatasState(int type) {
        boolean result = false;
        List<BXFile> munupdates = new ArrayList<BXFile>();
        List<BXFile> dates1 = getUnupdate(type);
        List<BXFile> dates2 = getUpdating(type);
        dates1.addAll(dates2);
        munupdates.addAll(dates1);

        if (munupdates.size() > 0) {

            DbFile.getDbInstance(mContext).updateDbInfos(0,
                    BXFile.FileStateSwitch(FileState.UPDATING), munupdates);

            for (BXFile mFile : munupdates) { // 数据交换更新
                AddUpdating(type, mFile); // 必须先添加，添加过程中会查找
                RomveUnupdate(type, mFile);
            }
            result |= true;
        }

        return result;
    }


}
