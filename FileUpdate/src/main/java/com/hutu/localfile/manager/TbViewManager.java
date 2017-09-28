/**
 * @Title: TopViewManager.java
 * @Package com.hutu.localfile.manager
 * @Description: TODO
 * @author Long Li
 * @date 2015-5-9 上午8:47:26
 * @version V1.0
 */
package com.hutu.localfile.manager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hutu.databases.DbFile;
import com.hutu.localfile.DefaultPage;
import com.hutu.localfile.LocalFileOp;
import com.hutu.localfile.manager.ListenerManager.ListernerInterface;
import com.hutu.localfile.util.Constants;
import com.hutu.localfile.util.Debug;
import com.hutu.localfile.util.FileUtils;
import com.hutu.localfile.util.HProgressBar;
import com.hutu.localfile.util.Utils;
import com.hutu.localfileupdate.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hutu.localfile.manager.ListenerManager.UNUPDATEDNUM;
import static com.hutu.localfile.manager.ListenerManager.UPDATEDNUM;
import static com.hutu.localfile.manager.ListenerManager.UPDATINGNUM;

/**
 * TODO<请描述这个类是干什么的>
 *
 * @author Long Li
 * @data: 2015-5-9 上午8:47:26 g
 * @version: V1.0
 */
public class TbViewManager {

    private Button unupdateButton;
    private Button updatingButton;
    private Button updatedButton;

    private LinearLayout localfile_top;

    // private GridView unGridView = null; // 未上传的图片
    private ListView unupdateView = null;
    private ListView updatingView;
    private ListView updatedView;
    private ListView dateListView;

    private TextView emptyView;
    private TextView curDir;
    private static Button delButton, upButton, moveButton; // 底部手动上传
    // 移动
    // 删除按钮

    private static Button selectAllButton; // 底部全选按钮

    private List<BXFile> allFiles; // 所有的本地文件
    private List<BXFile> selectAllButtonChoosedFiles;

    private String startPath = "/";// 初始path
    private File curFile;// 当前目录
    private int firstImageFileIndex;// 第一个图片文件的index(滚动时只对于普通文件loadImage)
    private LocalFileAdapter localAdapter; // 显示所有的音视频以及图片文件

    private UpdateManager mUpdateManager; // 上传管理器

    private int OpType = 0; // 文件分类标示

    private int UNUPDATED = 1;
    private int UPDATING = 2;
    private int UPDATED = 3;
    private static int UserType = 1; // 模式

    private int REFRESH = 4; // 刷新页面
    private int BOTTOMVIEW = 5; // 底部button显示刷新
    public static Context mContext;

    private View baseView;
    public static DataManager mDataManager;

    // private int gridSize;
    private String TAG = "TbViewManager";
    private ListenerManager mListenerManager;

    public static int mOpreType = BXFile.LOCAL_MODE;
    public static View currentView;

    private BaseAdapter adapter;
    private UpdateFileAdapter updateFileAdapter;

    /**
     * 这个变量来判断是否是在上传中界面点了全选按钮， 解决了在 上传中的时候 点了全选 然后界面刷新 全选按钮的文字和图片就变了的bug
     * 主要是在刷新界面的函数里面
     */
    public static boolean ifInUpdatingPage = false;
    public static boolean ifInUpdatedPage = false;
    public static int defalutPageItemOrder = 10; // 随便给它一个默认的值
    public static int tempOrder = 8; // 增加一个变量，用它记录从哪个目录进来的

    public static boolean pauseAllIsClicked = false;




    public TbViewManager(Context context, int Type) {
        this.mContext = context;
        OpType = Type;
        mUpdateManager = UpdateManager.getInstance(mUpdatingHander, mContext);
        mDataManager = DataManager.getInstance(mContext);
        mListenerManager = new ListenerManager(this, mContext, mHandler,
                mUpdateManager);
        mListenerManager.setListernerInterface(mListernerInterface);
    }

    /**
     * @return the allFiles
     */
    public List<BXFile> getAllFiles() {
        return allFiles;
    }

    /**
     * @return the curFile
     */
    public File getCurFile() {
        return curFile;
    }

    /**
     * @return the userType
     */
    public int getUserType() {
        return UserType;
    }

    /**
     * @param userType the userType to set
     */
    public void setUserType(int userType) {
        UserType = userType;
    }

    /**
     * @return the opType
     */
    public  int getOpType() {
        return OpType;
    }

    public void setOpType(int type) {
        if ((type >= 0) && (type <= 4))
            OpType = type;
        showDataViews(1);
    }

    // 所有上传状态的更新在此hander
    public static Handler mUpdatingHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // int type = getOptype();
            int OpType = mOpreType;
            if (10 == msg.what) {
                BXFile mBxFile = (BXFile) msg.obj;
                if (null == mBxFile) {
                    Log.d("longli", "mBxfile is null");
                }
                ListView updatingView = null;

                if (OpType == BXFile.LOCAL_MODE) {
                    updatingView = LocalFileOp.getUpdatingView();
                } else {
                    updatingView = LocalDefaultFile.getUpdatingView();
                }

                if (null != updatingView) {
                    View mView = updatingView.findViewWithTag(mBxFile
                            .getFilePath());
                    if (mView != null) {
                        HProgressBar mBar = (HProgressBar) mView
                                .findViewById(R.id.id_progressbar01);
                        if (mBxFile.getFileProgress() <= 100) {
                            mBar.setProgress(mBxFile.getFileProgress());
                        }
                    }
                }

                if (mBxFile.getFileProgress() == 100) {
                    if (mDataManager == null) {
                        mDataManager = DataManager.getInstance(mContext);
                    }
                    mDataManager.addUpdated(OpType, mBxFile);
                    mDataManager.RomveUpdating(OpType, mBxFile);
                    updateViewTitleInfo(OpType, currentView, false);

                        Log.i("QAAAAAA",Utils.getPreferences("file_setting").toString()+"mBxFile.getFileProgress()"+mBxFile.getFileProgress());
                    if (Utils.getPreferences("file_setting").equals("1")) {   // delet
                        FileUtils.delete(mBxFile.getFilePath());
                        DbFile.getDbInstance(mContext).deletInfo(
                                mBxFile.getFileName(), mBxFile.getFilePath());
                    }
                }
            } else if (9 == msg.what) {
                Context context = (Context) msg.obj;
                // 注销掉了提示 请检查网络的吐司
                // Toast.makeText(context,
                // context.getString(R.string.errnetwork),
                // Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    public static Handler mViewHandler = mUpdatingHander;

    // 用与数据更新
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (UNUPDATED == msg.what) {
                if (OpType == BXFile.LOCAL_MODE) {
                    Log.d("TbViewManager", "UNUPDATED=" + UNUPDATED + "OpType="
                            + OpType);
                    updateViewTitleInfo(OpType, baseView, false);
                    setLocalData();

                } else {
                    showDataViews(UNUPDATED);
                }
            } else if (UPDATING == msg.what) {
                showDataViews(UPDATING);
                // 开始上传数据
            } else if (UPDATED == msg.what) {
                showDataViews(UPDATED);
            } else if (REFRESH == msg.what) {
                updateViewTitleInfo(OpType, baseView, false);
            } else if (BOTTOMVIEW == msg.what) { // BOTTOMVIEW
                // 更新页面显示
                if (2 == msg.arg1) {
                    moveButton.setText(mContext.getString(R.string.pastfile));
                } else {
                    InitLocalData("/");
                }
            }
            super.handleMessage(msg);
        }
    };

    // 刷新页面
    public static synchronized void updateViewTitleInfo(int type, View mView,
                                                        boolean flag) {
        if (mView == null) {
            return;
        }


        if (ifInUpdatingPage) { // 要是在上传中界面就暂时不更新按钮状态

        } else {
            if (ifInUpdatedPage) { // 这样的话在 已上传界面 全选 刷新之后 按钮图标和文字不会变了


            } else {

                //要是在未上传界面 就不自动刷新 防止 刷新的时候 全选按钮变成了 取消全选，一刷新没了
                if (ListenerManager.buttonID == 1) {


                } else {
                    setSelectAllButtonToNormal(); // 加上这句之后在未上传界面点击全选之后变成 取消全选之后
                    // 再去别的界面
                }


            }

            // 全选的按钮和文字了
        }

        currentView = mView;
        mOpreType = type;

        List<BXFile> unupdatedata = mDataManager.getUnupdate(type);
        List<BXFile> updatingdata = mDataManager.getUpdating(type);
        List<BXFile> updateddata = mDataManager.getUpdated(type);
        // 未上传 上传中 已上传三个
        Button unupdateButton = (Button) mView.findViewById(R.id.unupdate);
        Button updatingButton = (Button) mView.findViewById(R.id.updating);
        Button updatedButton = (Button) mView.findViewById(R.id.updated);

        Constants.itemUnmUnupdate=unupdatedata.size();
        Constants.itemUnmUnupding=updatingdata.size();
        Constants.itemUnmUnuped=updateddata.size();

        ListView unupdateView = (ListView) mView
                .findViewById(R.id.unupdateView);
        ListView updatingView = (ListView) mView
                .findViewById(R.id.updatingView);
        ListView updatedView = (ListView) mView.findViewById(R.id.updatedView);

        if (flag) { // 每次跳转到本地模式初始化button
            unupdateButton.setBackgroundColor(Color.parseColor("#009CFF"));
            updatingButton.setBackgroundColor(Color.parseColor("#00000000"));
            updatedButton.setBackgroundColor(Color.parseColor("#00000000"));
            if (type != BXFile.LOCAL_MODE) {
                UserType = 1;
                unupdateView.setVisibility(View.VISIBLE);
                updatingView.setVisibility(View.GONE);
                updatedView.setVisibility(View.GONE);
            } else {
                ListenerManager.bottomButtonShow(1);
            }
        }

        if (type == BXFile.LOCAL_MODE) { // 5
            LinearLayout mlLayout = (LinearLayout) currentView
                    .findViewById(R.id.default_bottom);
            if ((Utils.getDefautDirOp() == null)
                    || (Utils.getDefautDirOp().length() == 0)) {
                mlLayout.setVisibility(View.VISIBLE);
            } else {
                mlLayout.setVisibility(View.GONE);
            }
        }

        unupdateButton.setText(String.format(mContext
                        .getString(R.string.unupdate),
                (unupdatedata != null) ? unupdatedata.size() : 0));

        updatingButton.setText(String.format(mContext
                        .getString(R.string.updating),
                (updatingdata != null) ? updatingdata.size() : 0)); // 循环更新显示的条目

        updatedButton.setText(String.format(mContext
                        .getString(R.string.updated),
                (updateddata != null) ? updateddata.size() : 0));

        if (BXFile.LOCAL_MODE != type) { // 本地模式的未上传页面不需要刷新
            LocalFileAdapter unupdate_adapter = (LocalFileAdapter) unupdateView
                    .getAdapter();

            if (null != unupdate_adapter) {
                unupdate_adapter.refresh(unupdatedata);
                if ((unupdatedata.size() == 0) && (UserType == 1)) {
                    unupdateView.setVisibility(View.GONE);
                    setEmptyView(type,
                            mContext.getString(R.string.fileunupdate),
                            currentView);
                }
            }
        }

        UpdateFileAdapter updating_adapter = (UpdateFileAdapter) updatingView
                .getAdapter();

        LocalFileAdapter updated_adapter = (LocalFileAdapter) updatedView
                .getAdapter();

        if (null != updating_adapter) {
            updating_adapter.refresh(updatingdata); // 点上传中按钮的时候执行这里了

        }
        if ((updating_adapter == null) || (updatingdata.size() == 0)) {
            if (UserType == 2) {
                updatingView.setVisibility(View.GONE);
                setEmptyView(type, mContext.getString(R.string.fileupdateing),
                        currentView);
            }
        }

        if (null != updated_adapter) {
            updated_adapter.refresh(updateddata);
            if ((updateddata.size() == 0) && (UserType == 3)) {
                updatedView.setVisibility(View.GONE);
                setEmptyView(type, mContext.getString(R.string.fileupdated),
                        currentView);
            }
        }
        Button upButton = (Button) currentView.findViewById(R.id.update_button);
        Button delButton = (Button) currentView.findViewById(R.id.delet_button);
        /*
         * Button autoButton = (Button)
		 * currentView.findViewById(R.id.autoup_button);
		 */
        Button moveButton = (Button) currentView.findViewById(R.id.cut_button);

        if (type != BXFile.LOCAL_MODE) {
            ListenerManager.bottomButtonShow(UserType);
        } else {
            // autoButton.setVisibility(View.GONE);
            int viewshow = View.VISIBLE;
            if (Utils.getMoveFiles().size() != 0) {
                viewshow = View.GONE;
                moveButton.setText(mContext.getString(R.string.pastfile));
            } else {
                moveButton.setText(mContext.getString(R.string.moveto));

            }
            // 加这么一句是为了让 在上传中 让 上传按钮和 移动按钮取消
            if (ListenerManager.buttonID == 3) {
                upButton.setVisibility(View.GONE);
            } else {
                upButton.setVisibility(viewshow); // 原来只有这一句，没有if判断
            }

            delButton.setVisibility(viewshow);
        }
    }

    // fixme:数据更新时，需要弹出loading圈，由于时间比较短，目前不需要做支持

    /**
     * @param typeview 根据typeview的不同 展示不同的数据 未上传 上传中和已上传
     */
    public void showDataViews(int typeview) {
        // int mtype = typeview;
        String typeinfo;
        List<BXFile> mdata = null;
        LocalFileAdapter mAdapter = null; // 影视频未上传 已上传
        UpdateFileAdapter uAdapter = null; // 上传中
        ListView mView = null;

        if (typeview == UNUPDATED) {
            mdata = mDataManager.getUnupdate(OpType);
            typeinfo = mContext.getString(R.string.fileunupdate);
            mView = unupdateView;
            if (mView != null) {
                mAdapter = (LocalFileAdapter) mView.getAdapter();
            }

        } else if (typeview == UPDATING) {
            mdata = mDataManager.getUpdating(OpType);
            mView = updatingView;
            uAdapter = (UpdateFileAdapter) mView.getAdapter();
            typeinfo = mContext.getString(R.string.fileupdateing);
        } else if (typeview == UPDATED) {
            mdata = mDataManager.getUpdated(OpType);
            mView = updatedView;
            mAdapter = (LocalFileAdapter) mView.getAdapter();
            typeinfo = mContext.getString(R.string.fileupdated);
        } else {
            return;
        }
        if (unupdateView != null) {
            unupdateView.setVisibility((typeview == UNUPDATED) ? View.VISIBLE
                    : View.GONE);
        }
        if (updatingView != null) {
            updatingView.setVisibility((typeview == UPDATING) ? View.VISIBLE
                    : View.GONE);
        }
        if (updatedView != null) {
            updatedView.setVisibility((typeview == UPDATED) ? View.VISIBLE
                    : View.GONE);
        }


        if ((null != mdata) && (mdata.size() != 0)) {
            emptyView.setVisibility(View.GONE);
            if ((null == mAdapter) || (null == uAdapter)) {

                setViewAdapter(typeview, mdata, mView);
            } else {
                if (typeview == UPDATING) {
                    uAdapter.refresh(mdata);
                    mAdapter.refresh(mdata);
                }
            }

        } else {
            if (mView != null) {
                mView.setVisibility(View.GONE);
            }

            setEmptyView(OpType, typeinfo, baseView);
        }
        updateViewTitleInfo(OpType, baseView, false);
    }

    private void setViewAdapter(int typeview, List<BXFile> mdata, ListView mView) {

        if (typeview == UPDATING) {
            UpdateFileAdapter uAdapter = new UpdateFileAdapter(mdata, mContext,
                    mListenerManager.PstartListener,
                    mListenerManager.getSyncImageLoader(),
                    mListenerManager.imageLoadListener);
            Log.d(TAG, "update set adapter start");
            mView.setAdapter(uAdapter);

            mView.setOnItemClickListener(mListenerManager.mItemClickListener);
            mView.setOnItemLongClickListener(mListenerManager.mLongClickListener); // 设置上传中长按显示

        } else {
            LocalFileAdapter mAdapter = new LocalFileAdapter(mdata, mContext,
                    mListenerManager.getSyncImageLoader(),
                    mListenerManager.imageLoadListener);
            mView.setAdapter(mAdapter);

            mView.setOnItemClickListener(mListenerManager.mItemClickListener);
            mView.setOnItemLongClickListener(mListenerManager.mLongClickListener);
        }

    }

    private static void setEmptyView(int type, String info, View v) {
        TextView emptyView = (TextView) v.findViewById(R.id.emptyView);
        emptyView.setVisibility(View.VISIBLE);
        String mText = String.format(
                mContext.getString(R.string.curNofiles),
                (type == BXFile.LOCAL_MODE) ? mContext
                        .getString(R.string.noUpdateFile) : mContext
                        .getString(DefaultPage.default_ids[type]), info);

        emptyView.setText(mText);
    }

    /**
     * 本地文件查看模式初始化
     */
    public void setLocalData() {
        // TODO Auto-generated method stub
        startPath = "/";
        if (!FileUtils.isDir(startPath)) {
            startPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();

        }else {
            //后加的else   暂时先这样
            startPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        }

        UserType = UNUPDATED;
        // 隐藏其他两个页面数据
        updatingView.setVisibility(View.GONE);
        updatedView.setVisibility(View.GONE);

        InitLocalData(startPath); // 初始化本地显示数据
    }

    // 找到第一个图片类型文件index
    private void initFirstFileIndex() {
        firstImageFileIndex = -1;
        for (int i = 0; i < allFiles.size(); i++) {
            BXFile f = allFiles.get(i);
            if (!f.isDir() && f.getMimeType().equals(BXFile.MimeType.IMAGE)) {
                firstImageFileIndex = i;
                return;
            }
        }
    }

    // 本地模式数据初始化 问题：本地删除文件，在default模式下面肯能会继续出现(bug)
    public void InitLocalData(String dirPath) {
        curDir.setText(dirPath);
        curFile = new File(dirPath);
        File[] childs = curFile.listFiles();
        if (null == childs || 0 == childs.length) {
            unupdateView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            unupdateView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            if (null != allFiles)
                allFiles.clear();
            else
                allFiles = new ArrayList<BXFile>();

            for (File f : childs) {
                BXFile.Builder builder = new BXFile.Builder(f.getAbsolutePath());
                BXFile bxfile = builder.build();
                if (null != bxfile)
                    if (!mDataManager.checkLocalFileState(bxfile)) {
                        allFiles.add(bxfile);
                    }
            }
            Debug.debuger("所有文件的个数" + allFiles.size());
            Collections.sort(allFiles);
            initFirstFileIndex();
            SyncImageLoader syncImageLoader = mListenerManager
                    .getSyncImageLoader();
            if (null == localAdapter) {
                syncImageLoader.restore();
                localAdapter = new LocalFileAdapter(allFiles, mContext,
                        syncImageLoader, mListenerManager.imageLoadListener);
                unupdateView.setAdapter(localAdapter);

                unupdateView
                        .setOnScrollListener(mListenerManager.onScrollListener);
                mListernerInterface.loadImage();
            } else {
                Log.d(TAG, "刷新localAdapter");
                syncImageLoader.restore();
                mListernerInterface.loadImage();
                localAdapter.refresh(allFiles);
                unupdateView.setSelection(0);
            }
        }

        // autoButton.setVisibility(View.GONE);
        int viewshow = View.VISIBLE;
        if (Utils.getMoveFiles().size() != 0) {
            viewshow = View.GONE;
            moveButton.setText(mContext.getString(R.string.pastfile));
        } else {
            moveButton.setText(mContext.getString(R.string.moveto));

        }
        upButton.setVisibility(viewshow);
        delButton.setVisibility(viewshow);
    }
    // 本地模式数据初始化 问题：本地删除文件，在default模式下面肯能会继续出现(bug)
    public void InitLocalData2(String dirPath) {
        curDir.setText(dirPath);
        curFile = new File(dirPath);
        File[] childs = curFile.listFiles();
        if (null == childs || 0 == childs.length) {
            unupdateView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            unupdateView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            if (null != allFiles)
                allFiles.clear();
            else
                allFiles = new ArrayList<BXFile>();

            for (File f : childs) {
                BXFile.Builder builder = new BXFile.Builder(f.getAbsolutePath());
                BXFile bxfile = builder.build();
                if (null != bxfile)
                    if (!mDataManager.checkLocalFileState(bxfile)) {
                        if (f.getName().endsWith(".jpg")||f.getName().endsWith(".png")||f.getName().endsWith(".mp4")||f.getName().endsWith(".3gp")||f.getName().endsWith(".mpg")||f.getName().endsWith(".mpeg")||f.getName().endsWith(".mov")||f.getName().endsWith(".mp3")||f.getName().endsWith(".wmv")||f.getName().endsWith(".avi")||f.getName().endsWith(".rmvb")||f.getName().endsWith(".rm")||f.getName().endsWith(".mkv")||f.getName().endsWith(".asf")||f.getName().endsWith(".asx")) {
                            allFiles.add(bxfile);
                        }

                    }
            }
            Debug.debuger("所有文件的个数" + allFiles.size());
//            Log.i("qweqwe",""+allFiles.size());
            Collections.sort(allFiles);
            initFirstFileIndex();
            SyncImageLoader syncImageLoader = mListenerManager
                    .getSyncImageLoader();
            if (null == localAdapter) {
                syncImageLoader.restore();
                localAdapter = new LocalFileAdapter(allFiles, mContext,
                        syncImageLoader, mListenerManager.imageLoadListener);
                unupdateView.setAdapter(localAdapter);

                unupdateView
                        .setOnScrollListener(mListenerManager.onScrollListener);
                mListernerInterface.loadImage();
            } else {
                Log.d(TAG, "刷新localAdapter");
                syncImageLoader.restore();
                mListernerInterface.loadImage();
                localAdapter.refresh(allFiles);
                unupdateView.setSelection(0);
            }
        }

        // autoButton.setVisibility(View.GONE);
        int viewshow = View.VISIBLE;
        if (Utils.getMoveFiles().size() != 0) {
            viewshow = View.GONE;
            moveButton.setText(mContext.getString(R.string.pastfile));
        } else {
            moveButton.setText(mContext.getString(R.string.moveto));

        }
        upButton.setVisibility(viewshow);
        delButton.setVisibility(viewshow);
    }
    public void InitTopViewList(View v) {

        baseView = v;
        unupdateView = (ListView) v.findViewById(R.id.unupdateView);

        if (OpType == BXFile.LOCAL_MODE) {
            unupdateView
                    .setOnItemClickListener(mListenerManager.mItemClickListener); // 点击文件的时候进入文件夹
            unupdateView
                    .setOnItemLongClickListener(mListenerManager.mItemLongClickListener); // 长按选择目录？
            LinearLayout mlLayout = (LinearLayout) v
                    .findViewById(R.id.default_bottom); // 这个是底部的（上传，移动到，删除）三个按钮
            // 现在要做的是把上面的控件的ID找到，给隐藏试试

            if ((Utils.getDefautDirOp() == null)
                    || (Utils.getDefautDirOp().length() == 0)) {
                mlLayout.setVisibility(View.VISIBLE);
            } else {
                mlLayout.setVisibility(View.GONE);
            }
        }

        // curDir是啥？ 是（未上传 上传中 已上传）的这个textview 经过测试curDir不是那个空间
        curDir = (TextView) v.findViewById(R.id.curDir);
        // curDir已经设置GONE了，为啥还在呢？ 这是一个问题，要不要DEBUG一下
        curDir.setVisibility(View.GONE);

        emptyView = (TextView) v.findViewById(R.id.emptyView);

        updatingView = (ListView) v.findViewById(R.id.updatingView);
        updatingView.setVisibility(View.GONE);

        updatedView = (ListView) v.findViewById(R.id.updatedView);
        updatedView.setVisibility(View.GONE);

        unupdateButton = (Button) v.findViewById(R.id.unupdate);
        unupdateButton.setOnClickListener(mListenerManager.myClickListern);
        unupdateButton.setText(String.format(
                mContext.getString(R.string.unupdate), 0));

        updatingButton = (Button) v.findViewById(R.id.updating);
        updatingButton.setOnClickListener(mListenerManager.myClickListern);
        updatingButton.setText(String.format(
                mContext.getString(R.string.updating), 0));

        updatedButton = (Button) v.findViewById(R.id.updated);
        updatedButton.setOnClickListener(mListenerManager.myClickListern);
        updatedButton.setText(String.format(
                mContext.getString(R.string.updated), 0));

        unupdateButton.setBackgroundColor(Color.parseColor("#009CFF"));
        updatingButton.setBackgroundColor(Color.parseColor("#00000000"));
        updatedButton.setBackgroundColor(Color.parseColor("#00000000"));

        // 初始化 全选 Button
        selectAllButton = (Button) v.findViewById(R.id.select_all);
        selectAllButton.setOnClickListener(checkboxOnClickListener);
        selectAllButton.setVisibility(View.VISIBLE);
        // selectAllButton.setOnClickListener(mListenerManager.myClickListern);

        upButton = (Button) v.findViewById(R.id.update_button);
        upButton.setOnClickListener(mListenerManager.myClickListern);

        delButton = (Button) v.findViewById(R.id.delet_button);
        delButton.setOnClickListener(mListenerManager.myClickListern);

        moveButton = (Button) v.findViewById(R.id.cut_button);
        moveButton.setOnClickListener(mListenerManager.myClickListern);

        mListenerManager.sendTypeMessage(UNUPDATED);
        if (OpType == BXFile.LOCAL_MODE) {
            setLocalData();
            // autoButton.setVisibility(View.GONE);
            if (Utils.getMoveFiles().size() != 0) {
                upButton.setVisibility(View.GONE);
                delButton.setVisibility(View.GONE);
                moveButton.setText(mContext.getString(R.string.pastfile));
            }
        }
    }

    ListernerInterface mListernerInterface = new ListernerInterface() {

        @Override
        public void loadImage() {
            // TODO Auto-generated method stub
            int start = 0;
            int end = 0;
            List<BXFile> unupdatedata = mDataManager.getUnupdate(OpType);
            if (OpType == BXFile.LOCAL_MODE) {
                start = unupdateView.getFirstVisiblePosition();
                end = unupdateView.getLastVisiblePosition();
                if (end < firstImageFileIndex) {
                    Log.i(TAG, "loadImage return");
                    return;
                }
                if (start < firstImageFileIndex)
                    start = firstImageFileIndex;
                if (end >= allFiles.size()) {
                    end = allFiles.size() - 1;
                }
            }
            SyncImageLoader syncImageLoader = mListenerManager
                    .getSyncImageLoader();
            syncImageLoader.setLoadLimit(start, end);
            syncImageLoader.unlock();
        }
    };

    // 程序退出时，清除数据
    public void clear() {
        List<BXFile> unupdatedata = mDataManager.getUnupdate(OpType);
        List<BXFile> updatingdata = mDataManager.getUpdating(OpType);
        List<BXFile> updateddata = mDataManager.getUnupdate(OpType);

        if (null != unupdatedata) {
            unupdatedata.clear();
            unupdatedata = null;
        }

        if (null != updatingdata) {
            updatingdata.clear();
            updatingdata = null;
        }

        if (null != updateddata) {
            updateddata.clear();
            updateddata = null;
        }
        mHandler = null;
    }

    public void onBackPressed() {
        if (!startPath.equals(curFile.getAbsolutePath())) {
            InitLocalData(curFile.getParentFile().getAbsolutePath());
        }
    }

    /**
     * 全选和暂停 button 的 点击事件
     * 一开始考虑用的checkbox然后发现不好用，于是又改成button，checkboxOnClickListener也懒的改名字了
     */
    OnClickListener checkboxOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.select_all: // 底部全选 button

                    selectAllButton = (Button) v.findViewById(R.id.select_all);
                    String selectAllShownText = "全选";
                    // 获取全选按钮的状态 执行相应的方法
                    if (selectAllShownText.equals(selectAllButton.getText())) {
                        Log.i("QWEQWE","此全选菲比全选");
                        dealSelectAll();
                        UNUPDATEDNUM=3;
                        UPDATINGNUM=3;
                        UPDATEDNUM=3;
                    } else {
                        UNUPDATEDNUM=0;
                        UPDATINGNUM=0;
                        UPDATEDNUM=0;
                        dealDesSelectAll();
                    }

                    break;

            }
        }
    };

    /**
     * 全选的时候处理事件
     */

    private void dealSelectAll() {

        // 未上传 中 userType=1,opType=5
        // 上传中 userType=2,opType=5
        // 已上传 userType=3,opType=5

        // OpType是 默认 界面 五个默认目录 [0,1,2,3,4] 手动上传是 5

        // 要是在默认下面进去的话
        if (!ListenerManager.isLocalButtonClicked) {
            if (ListenerManager.buttonID == 1) {
                defalutPageItemOrder = tempOrder;
            }
        }

        int OpType = getOpType();
        int UserType = getUserType();
        DataManager mDataManager = DataManager.getInstance(mContext);
        List<BXFile> allFiles = getAllFiles();

        Log.d(TAG, "onItemClick is start UserType is ..." + UserType
                + ", OpType is " + OpType);
        // TODO Auto-generated method stub
        List<BXFile> dateList = null;
        BXFile bxfile = null;

        System.out.println("22 select    defalutPageItemOrder=="
                + defalutPageItemOrder + ";" + "ListenerManager.buttonID=="
                + ListenerManager.buttonID + ";"
                + "TbViewManager.ifInUpdatedPage=="
                + TbViewManager.ifInUpdatedPage);

        Log.d("order",
                "defalutPageItemOrder==" + String.valueOf(defalutPageItemOrder));

        // 要是从默认目录五个条目进来的话重新给OpType赋值
        switch (defalutPageItemOrder) {
            case 0:
//                Log.i("QWEQWE","要是从默认目录五个条目进来的话重新给OpType赋值"+unupdateView.getCount());
                OpType = 0;
                Log.d(TAG, "OpType=0");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[0])); // 加上这句之后
                // 全选的时候不崩溃了
                // 可能是因为没有初始化adapter

                tempOrder = 0;
                break;
            case 1:
                OpType = 1;
                Log.d(TAG, "OpType=1");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[1]));
                tempOrder = 1;
                break;
            case 2:
                OpType = 2;
                Log.d(TAG, "OpType=2");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[2]));
                tempOrder = 2;
                break;
            case 3:
                OpType = 3;
                Log.d(TAG, "OpType=3");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[3]));
                tempOrder = 3;
                break;
            case 4:
                OpType = 4;
                Log.d(TAG, "OpType=4");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[4]));
                tempOrder = 4;
                break;

        }
        Log.d(TAG, "重新赋值后  onItemClick is start UserType is ..." + UserType
                + ", OpType is " + OpType + "," + "tempOrder==" + tempOrder);

        switch (ListenerManager.buttonID) {

            case 0: // 从 默认 界面 点击 五个 默认目录 中随意一个进来
                dateListView = unupdateView;
                adapter = localAdapter;
                dateList = mDataManager.getUnupdate(OpType);

                dateListView.setAdapter(adapter);
                System.out
                        .println("Switch-case0-dateList.size==" + dateList.size());
                Log.d(TAG, "ListenerManager.buttonID==0");
                Log.i("qweqweS","@"+dateList.size());
                break;

            case 1: // 从本地进来 再点 未上传 界面
                Log.d(TAG, "CASE 1");
                dateListView = unupdateView; // 把未上传的listview赋值给dateListView
                adapter = localAdapter;

                dateList = mDataManager.getUnupdate(OpType);
                System.out
                        .println("Switch-case1-dateList.size==" + dateList.size());
                Log.d(TAG, "ListenerManager.buttonID==1");
                dateListView.setAdapter(adapter); // just added 10.31.2016
                break;
            case 2: // 上传中界面

                updateFileAdapter = null;
                dateListView = updatingView; // 把上传中的listview赋值给dateListView
                if (dateListView == null) {
                    return;
                }
                updateFileAdapter = (UpdateFileAdapter) dateListView.getAdapter(); // 得到上传中的适配器
                adapter = updateFileAdapter;
                dateList = mDataManager.getUpdating(OpType);
                // 这个判断是防止 全选了 然后数据上完完毕了 再点崩溃
                if (dateList.isEmpty()) {
                    return;
                }
                Log.d(TAG, "ListenerManager.buttonID==2");
                break;
            case 3: // 已上传界面

                dateListView = updatedView;

                adapter = (LocalFileAdapter) dateListView.getAdapter(); // 已上传的adapter用的也是localFileAdapter,在这个类初始化出来的变量名是
                dateListView.setAdapter(adapter); // 果然是没有setAdapter的原因，set了之后就刷新的及时了，已上传界面
                dateList = mDataManager.getUpdated(OpType);

                if (dateList.isEmpty()) {
                    return;
                }
                Log.d(TAG, "ListenerManager.buttonID==3");
                break;

        }

        int realDateSize = 0;
        // 只有在手动上传进入的未 上传页面 展示的 条目才用dateListView 其他都用dateList 这样做解决了 上传中的时候
        // 点全选按钮之后 崩溃的bug （指针越界）
        if (ListenerManager.buttonID == 1) { // 只在是1的时候使用datelistview作为数据源

            if (ListenerManager.isLocalButtonClicked) {
                realDateSize = dateListView.getCount();
            } else {
                realDateSize = dateList.size();
            }

            System.out.println("if bottonID==1, realDateSize=" + realDateSize);
        } else {
            realDateSize = dateList.size();
        }

        for (int i = 0; i < realDateSize; i++) { // for (int i = 0; i <
            // dateListView.getCount();
            // i++) { dateList.size()

            View oneItemView = unupdateView.getChildAt(i);

            switch (ListenerManager.buttonID) {
                case 0:
                    Log.d(TAG, "for--ListenerManager.buttonID==0");
                    localAdapter.checkedALLMap.put(i, true);
                    localAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    Log.d(TAG, "for--ListenerManager.buttonID==1");

                    localAdapter.checkedALLMap.put(i, true);
                    localAdapter.notifyDataSetChanged();
                    break;
                case 2:

                    Log.d(TAG, "for--ListenerManager.buttonID==2");
                    updateFileAdapter.checkedALLMap.put(i, true);
                    updateFileAdapter.notifyDataSetChanged();
                    break;
                case 3:
                    localAdapter = (LocalFileAdapter) adapter;
                    Log.d(TAG, "for--ListenerManager.buttonID==3");
                    localAdapter.checkedALLMap.put(i, true);
                    localAdapter.notifyDataSetChanged();

                    break;

            }

            if ((OpType == BXFile.LOCAL_MODE) && (UserType == UNUPDATED)) {
                bxfile = allFiles.get(i);
            } else {
                bxfile = dateList.get(i);
            }
            if (bxfile.isDir()) { // 本地模式，点击文件夹
                Log.d(TAG, "全选的时候包含文件夹");
                return;
            } else {
                if (!bxfile.isUsefulType()) { // 如果是不是允许上传的文件，则不允许添加
                    // Utils.showShortMsg(R.string.errType);

                    return;
                } else if (mDataManager.checkLocalFileState(bxfile)) { // 如果文件在上传中或者是已上传，则不添加
                    // return;
                }
                List<BXFile> choosedFiles2 = BXFileManager.getInstance()
                        .getChoosedFiles(); // List<BXFile>

                selectAllButton.setText(R.string.desselect_all); // 点击全选Button然后文字变成
                Drawable top = mContext.getResources().getDrawable(
                        R.drawable.desselect_all);

                selectAllButton.setCompoundDrawablesWithIntrinsicBounds(null,
                        top, null, null);

                if (choosedFiles2.contains(bxfile)) {
                    // choosedFiles.remove(bxfile); //去掉这句就可以实现全选了
                    // 之前没有去掉选择一个然后全选的时候会有一个没有被选中
                    //
                } else {
                    choosedFiles2.add(bxfile);
                }
                Log.d(TAG, "choosed files is " + choosedFiles2.size());
                selectAllButtonChoosedFiles = choosedFiles2; // choosedFiles
            }

        }

    }


    /**
     * 取消全选的时候处理的事件
     */
    private void dealDesSelectAll() {

        // 要是在默认下面进去的话
        if (!ListenerManager.isLocalButtonClicked) {
            if (ListenerManager.buttonID == 1) {
                defalutPageItemOrder = tempOrder;
            }
        }

        // 默认目录进去之后 userType=0,opType=5
        // 未上传 中 userType=1,opType=5
        // 上传中 userType=2,opType=5
        // 已上传 userType=3,opType=5
        int OpType = getOpType();
        int UserType = getUserType();
        DataManager mDataManager = DataManager.getInstance(mContext);
        List<BXFile> allFiles = getAllFiles();
        Log.d(TAG, "onItemClick is start UserType is ..." + UserType
                + ", OpType is " + OpType);
        // TODO Auto-generated method stub
        List<BXFile> dateList = null;
        BXFile bxfile = null;
        BXFile bxFilefornow = null;
        // 要是从默认目录五个条目进来的话重新给OpType赋值

        System.out.println("22 desSelec" + "defalutPageItemOrder=="
                + defalutPageItemOrder + "-------"
                + "ListenerManager.buttonID==" + ListenerManager.buttonID
                + "TbViewManager.ifInUpdatedPage=="
                + TbViewManager.ifInUpdatedPage);

        switch (defalutPageItemOrder) {
            case 0:
                OpType = 0;
                Log.d(TAG, "OpType=0");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[0]));
                tempOrder = 0;
                break;
            case 1:
                OpType = 1;
                Log.d(TAG, "OpType=1");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[1]));
                tempOrder = 1;
                break;
            case 2:
                OpType = 2;
                Log.d(TAG, "OpType=2");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[2]));
                tempOrder = 2;
                break;
            case 3:
                OpType = 3;
                Log.d(TAG, "OpType=3");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[3]));
                tempOrder = 3;
                break;
            case 4:
                OpType = 4;
                Log.d(TAG, "OpType=4");
                InitLocalData2(Utils.getPreferences(Utils.DefaultPath[4]));
                tempOrder = 4;
                break;

        }

        // 看从哪个入口进来 执行相应的方法
        switch (ListenerManager.buttonID) {

            case 0: // 从 默认 界面 点击 五个 默认目录 中随意一个进来
                dateListView = unupdateView;
                adapter = localAdapter;
                dateList = mDataManager.getUnupdate(OpType);


                dateListView.setAdapter(adapter);
                System.out.println("dateList.size==" + dateList.size());
                Log.d(TAG, "ListenerManager.buttonID==0");
                break;

            case 1:

                dateListView = unupdateView; // 把未上传的listview赋值给dateListView
                adapter = localAdapter;
                dateList = mDataManager.getUnupdate(OpType);
                dateListView.setAdapter(adapter); // 10.31.2016
                break;
            case 2:

                updateFileAdapter = null;
                dateListView = updatingView; // 把上传中的listview赋值给dateListView
                if (dateListView == null) {
                    return;
                }
                updateFileAdapter = (UpdateFileAdapter) dateListView.getAdapter(); // 得到上传中的适配器
                adapter = updateFileAdapter;
                dateList = mDataManager.getUpdating(OpType);

                // 这个判断是在这样的情景下 上传界面点了全选的按钮，然后上传完了，要是没有数据了图标就变回去，然后返回，不执行下面的代码了
                if (dateList.isEmpty()) {
                    setSelectAllButtonToNormal();
                    return;
                }
                Log.d(TAG, String.valueOf(dateList.size()));
                break;
            case 3:
                dateListView = updatedView;

                adapter = (LocalFileAdapter) dateListView.getAdapter();
                dateListView.setAdapter(adapter); // 果然是没有setAdapter的原因，set了之后就刷新的及时了，已上传界面
                dateList = mDataManager.getUpdated(OpType);

                if (dateList.isEmpty()) {
                    return;
                }

                Log.d(TAG, "case 3");

        }

        int realDateSize = 0;
        if (ListenerManager.buttonID == 1) { // 只在是1的时候使用datelistview作为数据源
            if (ListenerManager.isLocalButtonClicked) {
                realDateSize = dateListView.getCount();
            } else {
                realDateSize = dateList.size();
            }
        } else {
            realDateSize = dateList.size();

        }

        for (int i = 0; i < realDateSize; i++) { // dateList.size()

            View oneItemView = unupdateView.getChildAt(i);

            switch (ListenerManager.buttonID) {
                case 0:
                    Log.d(TAG, "for--ListenerManager.buttonID==0");
                    localAdapter.checkedALLMap.put(i, false);
                    localAdapter.notifyDataSetChanged();
                    break;

                case 1:
                    localAdapter.checkedALLMap.put(i, false);
                    localAdapter.notifyDataSetChanged();
                    break;
                case 2: // 上传中界面做个判断 如果选中时时间过长 然后更新完数据了，再点反选就报错的bug
                    // if (selectAll == realDateSize) {
                    updateFileAdapter.checkedALLMap.put(i, false);
                    updateFileAdapter.notifyDataSetChanged();
                    Log.d(TAG, "执行到了for循环里面的case 2");
                    // } else {
                    // i = realDateSize;
                    // for (int j = 0; j < selectAll; j++) {
                    // updateFileAdapter.checkedALLMap.put(i, false);
                    // updateFileAdapter.notifyDataSetChanged();
                    // }
                    // Log.d(TAG, "执行了else,下一步就返回了");

                    // updateFileAdapter.notifyDataSetChanged();
                    // updateViewTitleInfo(UserType, updatingView, true);

                    // return;
                    // }

                    break;
                case 3:
                    localAdapter.checkedALLMap.put(i, false);
                    localAdapter.notifyDataSetChanged();

                    Log.d(TAG, "case 3--1");
                    break;

            }

            if ((OpType == BXFile.LOCAL_MODE) && (UserType == UNUPDATED)) {
                bxfile = allFiles.get(i);
            } else {
                bxfile = dateList.get(i);
            }
            if (bxfile.isDir()) { // 本地模式，点击文件夹
                Log.d(TAG, "全选的时候包含文件夹");
                return;
            } else {
                if (!bxfile.isUsefulType()) { // 如果是不是允许上传的文件，则不允许添加
                    // Utils.showShortMsg(R.string.errType);
                    return;
                } else if (mDataManager.checkLocalFileState(bxfile)) { // 如果文件在上传中或者是已上传，则不添加
                    // return;
                }
                List<BXFile> choosedFiles = BXFileManager.getInstance()
                        .getChoosedFiles();

                if (choosedFiles.contains(bxfile)) {
                    choosedFiles.remove(bxfile);

                    // if (fileCheckBox != null)
                    // fileCheckBox.setChecked(false);
                } else {
                    // choosedFiles.add(bxfile);

                    // if (fileCheckBox != null)
                    // fileCheckBox.setChecked(true);

                }
                Log.d(TAG, "choosed files is " + choosedFiles.size());
                selectAllButtonChoosedFiles = choosedFiles; // choosedFiles
            }

        }

        setSelectAllButtonToNormal();

    }

    /**
     * 把全选按钮设置成为初始状态
     */
    public static void setSelectAllButtonToNormal() {
        selectAllButton.setText(R.string.select_all); // 点击全选Button然后文字变成
        Drawable top = mContext.getResources().getDrawable(
                R.drawable.select_all);

        selectAllButton.setCompoundDrawablesWithIntrinsicBounds(null, top,
                null, null);
    }

    /**
     * 把全选按钮设置成为取消状态
     */
    public static void setSelectAllButtonToNormal2() {
        selectAllButton.setText(R.string.desselect_all); // 点击全选Button然后文字变成
        Drawable top = mContext.getResources().getDrawable(
                R.drawable.desselect_all);

        selectAllButton.setCompoundDrawablesWithIntrinsicBounds(null,
                top, null, null);
    }
    /**
     * @return 得到上传页面中的List<BXFile>
     */
    public List<BXFile> getUplodingList() {
        List<BXFile> dateList = null;
        DataManager mDataManager = DataManager.getInstance(mContext);
        dateList = mDataManager.getUpdating(5);
        System.out.println("getUplodingList==" + dateList.size());
        return dateList;
    }

    /**
     * 在上传中页面的时候隐藏 全选 按钮
     */
    // public static void hideSelectAllButton() {
    // selectAllButton.setVisibility(View.GONE);
    //
    // }
    public static void restoreSelectAllButton() {
        if (selectAllButton == null) {
            return;
        }
        selectAllButton.setVisibility(View.VISIBLE);
    }

    public static void hideUpAndMoveButton() {
        Log.d("hide", "hide buttons");
        upButton.setVisibility(View.GONE);
        moveButton.setVisibility(View.GONE);
    }


}
