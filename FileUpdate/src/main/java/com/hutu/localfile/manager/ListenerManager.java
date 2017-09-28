/**
 * @Title: ListenerManager.java
 * @Package com.hutu.localfile.manager
 * @Description: TODO
 * @author Long Li
 * @date 2015-6-2 下午4:21:33
 * @version V1.0
 */
package com.hutu.localfile.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hutu.databases.DbFile;
import com.hutu.localfile.manager.BXFile.FileState;
import com.hutu.localfile.manager.BXFile.UpdatingState;
import com.hutu.localfile.util.Constants;
import com.hutu.localfile.util.FileUtils;
import com.hutu.localfile.util.MIMEUtils;
import com.hutu.localfile.util.MyAlertDialogFragment;
import com.hutu.localfile.util.Utils;
import com.hutu.localfileupdate.MainActivity;
import com.hutu.localfileupdate.R;
import com.hutu.zhang.Constantx;

import java.io.File;
import java.util.List;

import static com.hutu.localfile.manager.UpdateManager.MyUpdaters;

public class ListenerManager {
    private SyncImageLoader syncImageLoader = null;
    private String TAG = "ListenerManager";
    private int UNUPDATED = 1;
    private int UPDATING = 2;
    private int UPDATED = 3;

    public static int UNUPDATEDNUM = 0;
    public static int UPDATINGNUM = 0;
    public static int UPDATEDNUM = 0;
    private static TbViewManager mTbViewManager = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private UpdateManager mUpdateManager = null;
    private int i = 1;
    private static Button selectAllButton; // 底部全选按钮
    public static int buttonID = 0;  //定义一个变量来记录是哪个按钮上面三个分别1，2，3；取消==5；删除==6.  默认是0 表示从 默认界面五个条目中随便一个进来

    public static boolean isLocalButtonClicked = false;  //记录本地按钮是否点过


    public ListenerManager(TbViewManager mTb, Context context,
                           Handler mHandler, UpdateManager mUpdateManager) {
        mTbViewManager = mTb;
        mContext = context;
        this.mHandler = mHandler;
        this.mUpdateManager = mUpdateManager;
        syncImageLoader = new SyncImageLoader();
    }

    public ListernerInterface mListernerInterface;

    /**
     * @return the syncImageLoader
     */
    public SyncImageLoader getSyncImageLoader() {
        return syncImageLoader;
    }

    public interface ListernerInterface {
        public void loadImage();
    }

    public void setListernerInterface(ListernerInterface mInterface) {
        mListernerInterface = mInterface;
    }

    AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    syncImageLoader.lock();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    mListernerInterface.loadImage();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    syncImageLoader.lock();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }
    };

    // 图片
    SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener() {
        @Override
        public void onImageLoad(String ImagePath, Integer t, Drawable drawable) {

            if (UNUPDATED == (mTbViewManager.getUserType())) {

                ListView unupdateView = (ListView) TbViewManager.currentView
                        .findViewById(R.id.unupdateView);
                View view = unupdateView.findViewWithTag(t);
                if (view != null) {
                    ImageView iv = (ImageView) view.findViewById(R.id.fileType);
                    iv.setImageDrawable(drawable);
                }

            } else {
                int viewid = R.id.updatingView;
                int tId = R.id.upfileType;

                if (mTbViewManager.getUserType() == UPDATED) {
                    viewid = R.id.updatedView;
                    tId = R.id.fileType;
                }

                ListView unupdateView = (ListView) TbViewManager.currentView
                        .findViewById(viewid);

                View view = null;

                if (mTbViewManager.getUserType() == UPDATING) {
                    if (ImagePath != null) {
                        view = unupdateView.findViewWithTag(ImagePath);
                    }
                } else {
                    view = unupdateView.findViewWithTag(t);
                }

                if (view != null) {
                    ImageView iv = (ImageView) view.findViewById(tId);
                    iv.setImageDrawable(drawable);
                }
            }
        }

        @Override
        public void onError(Integer t) {
            if (TbViewManager.mOpreType == BXFile.LOCAL_MODE) {
                ListView unupdateView = (ListView) TbViewManager.currentView
                        .findViewById(R.id.unupdateView);
                View view = unupdateView.findViewWithTag(t);
                if (view != null) {
                    ImageView iv = (ImageView) view.findViewById(R.id.fileType);
                    iv.setImageResource(R.drawable.bxfile_file_unknow);
                } else {
                    Log.i(TAG, " onError View not exists");
                }
            }
        }
    };

    // 音频、视频、图片长按事件
    OnItemLongClickListener mLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            // TODO Auto-generated method stub
            DataManager mDataManager = DataManager.getInstance(mContext);
            int OpType = mTbViewManager.getOpType();
            int UserType = mTbViewManager.getUserType();
            List<BXFile> updatedata = null;
            if (UserType == UNUPDATED) {
                updatedata = mDataManager.getUnupdate(OpType);
            } else if (UserType == UPDATING) {
                updatedata = mDataManager.getUpdating(OpType);
            } else if (UserType == UPDATED) {
                updatedata = mDataManager.getUpdated(OpType);
            }

            BXFile bxfile = updatedata.get(position);
            MIMEUtils mMiMEUTils = MIMEUtils.getInstance();
            mMiMEUTils.getPendingIntent((Activity) mContext,
                    new File(bxfile.getFilePath()));

            return true;
        }

    };

    // 上传页面中的 暂停和开始按钮响应事件
    OnClickListener PstartListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            LinearLayout layout = (LinearLayout) v.getParent();
            DataManager mDataManager = DataManager.getInstance(mContext);
            UpdateManager mUpdateManager = UpdateManager.getInstance(null,
                    mContext);
            int OpType = mTbViewManager.getOpType();

            TextView mPath = (TextView) ((LinearLayout) layout.getParent())
                    .findViewById(R.id.upfilePath);
            String mFilePath = (String) mPath.getText();
            BXFile mFile = mDataManager.getAllUpdating().get(mFilePath);
            if (mFile == null) {
                return;
            }
            Button pause_button = (Button) layout
                    .findViewById(R.id.pause_button);
            Button start_button = (Button) layout
                    .findViewById(R.id.start_button);
            switch (v.getId()) {
                case R.id.pause_button:  //暂停上传
                    start_button.setVisibility(View.VISIBLE);
                    v.setVisibility(View.GONE);
                    mUpdateManager.pause(mFile); // 停止
                    System.out.println("pauseUploadfile");
//                   点击开始上传
                    mDataManager.setUpdatingState(UpdatingState.pause, OpType,
                            mFile);
                    Log.i("qweqweqwe","==类型"+OpType+mFile.getFilePath());
                    break;

                case R.id.start_button:   //开始上传
//                   点击暂停上传
                    pause_button.setVisibility(View.VISIBLE);
                    v.setVisibility(View.GONE);
                    mDataManager.setUpdatingState(UpdatingState.start, OpType,
                            mFile);
                    mUpdateManager.start(mFile); // 开始
                    break;

                default:
                    break;
            }
        }
    };

    public static class NetStart extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("netstart.listion")) {
                DataManager mDataManager = DataManager.getInstance(context);
                UpdateManager mUpdateManager = UpdateManager.getInstance(
                        TbViewManager.mUpdatingHander, context);
                int OpType = mTbViewManager.getOpType();
                List<BXFile> updatingFiles = mDataManager
                        .getUpdating(BXFile.LOCAL_MODE); // 获取所有的上传中文件
                Toast.makeText(context, "ASD"+updatingFiles.size()+"==="+Constantx.isWork, Toast.LENGTH_SHORT).show();

                BXFile mFile = mDataManager.getAllUpdating().get(updatingFiles.get(0).getFilePath());
                MyUpdaters.remove(mFile.getFilePath());
                mDataManager.setUpdatingState(UpdatingState.pause, OpType,
                        mFile);
                Log.i("qweqweqwe",updatingFiles.get(0)+"==类型"+OpType+mFile.getFilePath());

                if (Constantx.isWork==2){
                    Toast.makeText(context, "ASDASD  isWork", Toast.LENGTH_SHORT).show();
                    mDataManager.setUpdatingState(UpdatingState.start, OpType,
                            mFile);
                    mUpdateManager.start(mFile); // 开始
                }
            } else {

            }
        }
    }


    /**
     * @param type 发送消息到 TbViewManager
     */
    public void sendTypeMessage(int type) {
        Message message = Message.obtain();
        message.what = type;
        mHandler.sendMessage(message);
        BXFileManager.getInstance().clear();
    }

    // 取消上传中的文件
    public void CancelChoseFiles() {
        LFBApplication app = (LFBApplication) ((Activity) mContext)
                .getApplication();
        app.execRunnable(new Runnable() {

            @Override
            public void run() {
                int mOpType = mTbViewManager.getOpType();
                List<BXFile> choosedFiles = BXFileManager.getInstance()
                        .getChoosedFiles();
                DataManager mDataManager = DataManager.getInstance(mContext);
                DbFile.getDbInstance(mContext).updateDbInfos(0,
                        BXFile.FileStateSwitch(FileState.UNUPDATE),
                        choosedFiles); // 更新数据库
                for (BXFile mFile : choosedFiles) { // 数据交换更新
                    // 如果文件正在上传，应该停止上传
                    UpdateManager.getInstance(null, mContext).delet(mFile);
                    mDataManager.AddUnupdate(mOpType, mFile);
                    mDataManager.RomveUpdating(mOpType, mFile);
                }
                sendTypeMessage(UPDATING);
            }
        });
    }

    // 上传选中的文件
    public void UpdateChoseFiles() {

        List<BXFile> choosedFiles = BXFileManager.getInstance()
                .getChoosedFiles();
        if (choosedFiles.size() <= 0) {
            Utils.showShortMsg(R.string.failedUpdate);
            return;
        }

        LFBApplication app = (LFBApplication) ((Activity) mContext)
                .getApplication();
        app.execRunnable(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                int OpType = mTbViewManager.getOpType();
                DataManager mDataManager = DataManager.getInstance(mContext);
                List<BXFile> choosedFiles = BXFileManager.getInstance()
                        .getChoosedFiles();

                if (choosedFiles.size() > 0) {

                    for (BXFile mFile : choosedFiles) { // 数据交换更新
                        mDataManager.AddUpdating(OpType, mFile); // 必须先添加，添加过程中会查找

                        mDataManager.RomveUnupdate(OpType, mFile);

                        List<BXFile> allFiles = mTbViewManager.getAllFiles();
                        if ((allFiles != null) && (allFiles.size() != 0)) {
                            allFiles.remove(mFile);
                            Log.i("QWEQWEQ", "1");
                        }
                        Log.i("QWEQWEQ", "2");

                    }
                    Log.i("QWEQWEQ", "3");
                    DbFile.getDbInstance(mContext).updateDbInfos(0,
                            BXFile.FileStateSwitch(FileState.UPDATING),
                            choosedFiles);
                    mUpdateManager.BatchUpdate(choosedFiles);// ??
                } else {
                    Log.i("QWEQWEQ", "4");
                }
                sendTypeMessage(UNUPDATED);
            }
        });
        Utils.showShortMsg(R.string.successUpdate);
    }

    // 删除选中的文件
    public void DeletChoseFiles() {

        LFBApplication app = (LFBApplication) ((Activity) mContext)
                .getApplication();
        app.execRunnable(new Runnable() {
            @Override
            public void run() {
                int mUserType = mTbViewManager.getUserType();
                int mOpType = mTbViewManager.getOpType();

                DataManager mDataManager = DataManager.getInstance(mContext);
                List<BXFile> choosedFiles = BXFileManager.getInstance()
                        .getChoosedFiles();

                for (BXFile mFile : choosedFiles) { // 数据更新
                    if (mUserType == UNUPDATED) {
                        mDataManager.RomveUnupdate(mOpType, mFile);
                    } else if (mUserType == UPDATING) {
                        mDataManager.RomveUpdating(mOpType, mFile);
                    } else if (mUserType == UPDATED) {
                        mDataManager.RomveUpdated(mOpType, mFile);
                    }
                    DbFile.getDbInstance(mContext).deletInfo(
                            mFile.getFileName(), mFile.getFilePath());
                    FileUtils.delete(mFile.getFilePath());
                }
                sendTypeMessage(mUserType);
            }
        });
        TbViewManager.setSelectAllButtonToNormal();
    }

    /*
     * 弹出删除对话框
     */
    private void showDeletDlag() {

        int title_id = R.string.p_choose_files;

        if (BXFileManager.getInstance().getChoosedFiles().size() > 0) {
            title_id = R.string.isDeletFile;
        }

        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(title_id))
                .setPositiveButton(mContext.getString(R.string.sure),
                        new DialogInterface.OnClickListener() {
                            @Override
                            //删除
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                if (BXFileManager.getInstance()
                                        .getChoosedFiles().size() > 0) {
                                    DeletChoseFiles();
                                }

                            }

                        })
                .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        //取消删除
                        System.out.println("22 buttonID==" + buttonID);
                    }

                })

                .show();

    }

    // 本地模式下面中长按时间处理
    OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            // TODO Auto-generated method stub

            int OpType = mTbViewManager.getOpType();
            DataManager mDataManager = DataManager.getInstance(mContext);
            List<BXFile> unupdatedata = mDataManager.getUnupdate(OpType);
            List<BXFile> allFiles = mTbViewManager.getAllFiles();
            BXFile bxfile = null;
            if (OpType == BXFile.LOCAL_MODE) {
                bxfile = allFiles.get(position);
            } else {
                bxfile = unupdatedata.get(position);
            }

            if ((Utils.getDefautDirOp() == null)
                    || (Utils.getDefautDirOp().length() == 0)) {
                return true;
            }

            DialogFragment newFragment = MyAlertDialogFragment.newInstance(
                    bxfile, mHandler, mContext);
            if (newFragment != null)
                newFragment.show(((Activity) mContext).getFragmentManager(),
                        "dialog");
            // 弹出对话框 删除 还是移动
            return true; // 防止触发短按事件
        }
    };

    /*
     * 处理上传按钮事件
     */
    private void dealUpdateButton(int UserType) {
        if ((TbViewManager.mOpreType == BXFile.LOCAL_MODE)
                && (UserType == UNUPDATED)) { // 本地模式下面粘贴操作
            String OpFile = Utils.getProperty(Utils.OpType);
            if ((OpFile != null) && (OpFile.length() != 0)
                    && (!OpFile.equals("OpType"))) {
                File srcFile = new File(Utils.getProperty(Utils.OpFile));
                File targFile = new File(mTbViewManager.getCurFile()
                        .getAbsoluteFile() + "/" + srcFile.getName());
                if (OpFile.equals(mContext.getString(R.string.cutfile))) {
                    FileUtils.moveFile(srcFile, targFile);
                } else if (OpFile.equals(mContext.getString(R.string.copyfile))) {
                    FileUtils.copyFile(srcFile, targFile);
                }
                mTbViewManager.InitLocalData(mTbViewManager.getCurFile()
                        .getAbsolutePath());


                bottomButtonShow(UNUPDATED);
                Button delButton = (Button) TbViewManager.currentView
                        .findViewById(R.id.delet_button);
                delButton.setVisibility(View.VISIBLE);
                Utils.setProperty(Utils.OpType, "OpType");
                return;
            }
        }
        if (UserType == UNUPDATED) {
            UpdateChoseFiles();

        } else if (UserType == UPDATING) { // 取消选中上传的文件
            CancelChoseFiles();
            TbViewManager.setSelectAllButtonToNormal(); //取消完然后让 全选按钮恢复默认
        }
    }

    /*
     * 处理设置自动或取消自动事件
     */
    /*
     * private void dealAutoUpAction(int type) { if (TbViewManager.mOpreType !=
	 * BXFile.LOCAL_MODE) { String title = ""; String mstate = Utils
	 * .getPreferences(Utils.DefaultPath[TbViewManager.mOpreType] + Utils.A);
	 * //String mstate = Utils.getPreferences(Utils.AutoMode); int id =
	 * R.string.isSetOutUpdate; if ((mstate != null) && (mstate.length() > 0)) {
	 * // 已经设定 id = R.string.isCancelOutUpdate; }
	 * 
	 * title = String .format(MainActivity.mContext.getString(id),
	 * MainActivity.mContext
	 * .getString(DefaultPage.default_ids[TbViewManager.mOpreType]));
	 * 
	 * new AlertDialog.Builder(MainActivity.mContext) .setTitle(title)
	 * .setPositiveButton( MainActivity.mContext.getString(R.string.sure), new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) { //
	 * TODO Auto-generated method stub String autoPath =
	 * Utils.DefaultPath[TbViewManager.mOpreType] + Utils.A; String mstate =
	 * Utils .getPreferences(autoPath); if ((mstate != null) && (mstate.length()
	 * > 0)) { // 取消设定 Utils.setPreferences(autoPath, ""); } else { // 设定自动上传目录
	 * Utils.setPreferences(autoPath, Utils.AutoMode); }
	 * 
	 * bottomButtonShow(UNUPDATED); }
	 * 
	 * }) .setNegativeButton( MainActivity.mContext.getString(R.string.cancel),
	 * null).show(); } }
	 */
    /*
     * 处理剪切粘贴操作
	 */
    private void dealMoveButton() {
        int move_id = R.string.p_choose_files;
        String move_tile = "";

        if (BXFileManager.getInstance().getChoosedFiles().size() > 0) {
            move_id = R.string.isMoveFiles;
        }

        move_tile = mContext.getString(move_id);

        if (Utils.getMoveFiles().size() != 0) {

            move_tile = String.format(
                    MainActivity.mContext.getString(R.string.isPasteFiles),
                    mTbViewManager.getCurFile().getName());
        }

        new AlertDialog.Builder(mContext)
                .setTitle(move_tile)
                .setPositiveButton(mContext.getString(R.string.sure),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                if (Utils.getMoveFiles().size() == 0) {
                                    if (BXFileManager.getInstance()
                                            .getChoosedFiles().size() > 0) {
                                        Utils.StoreMoveFiles(BXFileManager
                                                .getInstance()
                                                .getChoosedFiles());
//										SettingFra.sendBroadCastToCenter(
//												MainActivity.mContext, 1);  //不发送广播 这样 点粘贴就打开我写的 打开一个dialog选择目录那里了
                                    }
                                } else {
                                    if (!mTbViewManager.getCurFile().canWrite()) {
                                        new AlertDialog.Builder(mContext)
                                                .setTitle(
                                                        MainActivity.mContext
                                                                .getString(R.string.isFileWritePre))
                                                .setPositiveButton(
                                                        mContext.getString(R.string.sure),
                                                        null)
                                                .setNegativeButton(
                                                        mContext.getString(R.string.cancel),
                                                        null).show();
                                    } else {
                                        List<String> mlists = Utils
                                                .getMoveFiles();
                                        File srcFile = null;
                                        File targFile = null;
                                        String mtargpath = mTbViewManager
                                                .getCurFile().getAbsoluteFile()
                                                + "/";
                                        for (String file : mlists) {
                                            srcFile = new File(file);
                                            FileUtils.moveFile(file, mtargpath
                                                    + srcFile.getName());
                                        }
                                        Utils.cleanProperties();
                                        mTbViewManager.onBackPressed();
                                    }
                                }
                            }

                        })
                .setNegativeButton(mContext.getString(R.string.cancel), null)
                .show();
    }

    // 上下基本按钮操作  底部按钮点击操作
    OnClickListener myClickListern = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int UserType = mTbViewManager.getUserType();
            switch (v.getId()) {
                case R.id.update_button:
                    dealUpdateButton(UserType); // 处理点击 手动上传时候的函数
                    break;
                case R.id.delet_button:
                    showDeletDlag();
//				buttonID=6;
                    break;

                case R.id.cut_button:
                    dealMoveButton();
                    break;
                case R.id.unupdate: //本地顶部未上传按钮
                    TbViewManager.restoreSelectAllButton();
                    TbViewManager.defalutPageItemOrder = 10;   //从三个按钮进去的时候 初始化默认目录 这样就不会出现设置完默认目录之后 再点三大按钮 全选的时候会出现别的VIEW的情况了
                    TbViewManager.ifInUpdatingPage = false;
                    TbViewManager.setSelectAllButtonToNormal();
                    TbViewManager.ifInUpdatedPage = false;
                    buttonID = 1;
                    dealActionButton(v, UNUPDATED);
                    cleanNumStatus();
                    break;
                case R.id.updating: //本地顶部上传中按钮
//				TbViewManager.hideSelectAllButton();  //隐藏 全选 按钮
                    TbViewManager.defalutPageItemOrder = 10;
                    TbViewManager.ifInUpdatingPage = true;
                    TbViewManager.ifInUpdatedPage = false;
                    TbViewManager.setSelectAllButtonToNormal();
                    buttonID = 2;
                    dealActionButton(v, UPDATING);
                    cleanNumStatus();
                    break;
                case R.id.updated: //本地顶部已上传按钮
                    TbViewManager.restoreSelectAllButton();
                    TbViewManager.defalutPageItemOrder = 10;
                    TbViewManager.ifInUpdatingPage = false;
                    TbViewManager.ifInUpdatedPage = true;
                    TbViewManager.setSelectAllButtonToNormal();
                    buttonID = 3;
                    dealActionButton(v, UPDATED);
                    TbViewManager.hideUpAndMoveButton();
                    cleanNumStatus();
                    break;


            }
        }
    };

    private void cleanNumStatus() {
        UNUPDATEDNUM = 0;
        UPDATINGNUM = 0;
        UPDATEDNUM = 0;
    }

    public void dealActionButton(View v, int type) { // private
        mTbViewManager.setUserType(type);
        sendTypeMessage(type);
        bottomButtonShow(type);
        changeButtonBackGround(v, type);
    }

    /*
     * 按钮按下时，改变当前确认按钮的背景颜色
     */
    private void changeButtonBackGround(View v, int type) {
        LinearLayout mLayout = (LinearLayout) v.getParent();
        Button unupdateButton = (Button) mLayout.findViewById(R.id.unupdate);
        Button updatingButton = (Button) mLayout.findViewById(R.id.updating);
        Button updatedButton = (Button) mLayout.findViewById(R.id.updated);

        unupdateButton.setBackgroundColor(Color.parseColor("#00000000"));
        updatingButton.setBackgroundColor(Color.parseColor("#00000000"));
        updatedButton.setBackgroundColor(Color.parseColor("#00000000"));

        if (type == UNUPDATED) {
            unupdateButton.setBackgroundColor(Color.parseColor("#009CFF"));
        } else if (type == UPDATING) {
            updatingButton.setBackgroundColor(Color.parseColor("#009CFF"));
        } else {
            updatedButton.setBackgroundColor(Color.parseColor("#009CFF"));
        }
    }

    /*
     * 底部按钮更新状态 设置底部按钮们的隐藏还是显示还是文字的变化 等
     */
    public static void bottomButtonShow(int type) {
        int UNUPDATED = 1;
        int UPDATING = 2;
        int UPDATED = 3;

        int update_show_id = View.VISIBLE;
        int move_show_id = View.VISIBLE;
        int auto_show_id = View.GONE;

        Drawable update_drawable = MainActivity.mContext.getResources()
                .getDrawable(R.drawable.update);

        // Drawable auto_drawable = MainActivity.mContext.getResources()
        // .getDrawable(R.drawable.open);

        // maupdate
        String update_show_text = MainActivity.mContext
                .getString(R.string.cancel);

        if (type == UNUPDATED) {
            // update_show_text = MainActivity.mContext
            // .getString(R.string.updatefile);

            update_show_text = "手动上传";

			
			
			
			/*
             * if (TbViewManager.mOpreType != BXFile.LOCAL_MODE) { auto_show_id
			 * = View.VISIBLE; String mstate = Utils
			 * .getPreferences(Utils.DefaultPath[TbViewManager.mOpreType] +
			 * Utils.A); //String mstate = Utils.getPreferences(Utils.AutoMode);
			 * if ((mstate != null) && (mstate.length() > 0)) { // 已经设定,显示取消的图片
			 * auto_drawable = MainActivity.mContext.getResources()
			 * .getDrawable(R.drawable.close); }
			 * 
			 * }
			 */
        } else if (type == UPDATING) {
            update_drawable = MainActivity.mContext.getResources().getDrawable(
                    R.drawable.cance_update);
            move_show_id = View.GONE;


        } else if (type == UPDATED) {
            update_show_id = View.GONE;
            move_show_id = View.GONE;  //10.31.2016 隐藏移动到按钮

            //未上传界面 让 暂停/开始 checkbox 不显示 GONE

        }

        Button upButton = (Button) TbViewManager.currentView
                .findViewById(R.id.update_button);
        Button moveButton = (Button) TbViewManager.currentView
                .findViewById(R.id.cut_button);
		/*
		 * Button autoButton = (Button) TbViewManager.currentView
		 * .findViewById(R.id.autoup_button);
		 */
        //更改button的图标
        upButton.setCompoundDrawablesWithIntrinsicBounds(null, update_drawable,
                null, null);
        upButton.setVisibility(update_show_id);
        upButton.setText(update_show_text); // 这一句是把在上传中的时候 底部的按钮 更改名字的

        moveButton.setVisibility(move_show_id);
		
		

		/*
		 * autoButton.setVisibility(auto_show_id);
		 * autoButton.setCompoundDrawablesWithIntrinsicBounds(null,
		 * auto_drawable, null, null);
		 */
    }

    /**
     * 区分 单选全选
     *
     * @ c  checkbox控件
     */
    private void checkboxChannged(int biaoshi, CheckBox c) {
        if (c != null) {
            //未上传
            if (UNUPDATED == biaoshi) {
                if (c.isChecked()) {//未选中
                    UNUPDATEDNUM--;
                    if (UNUPDATEDNUM < Constants.itemUnmUnupdate) {
                        mTbViewManager.setSelectAllButtonToNormal();
                    }
                } else {//选中等于list数目是改变颜色
                    UNUPDATEDNUM++;
                    if (UNUPDATEDNUM == Constants.itemUnmUnupdate) {
                        mTbViewManager.setSelectAllButtonToNormal2();
                    }
                }

            }
            //上传中
            else if (UPDATING == biaoshi) {

                if (c.isChecked()) {//未选中
                    UPDATINGNUM--;
                    if (UPDATINGNUM < Constants.itemUnmUnupding) {
                        mTbViewManager.setSelectAllButtonToNormal();
                    }
                } else {//选中等于list数目是改变颜色
                    UPDATINGNUM++;
                    if (UPDATINGNUM == Constants.itemUnmUnupding) {
                        mTbViewManager.setSelectAllButtonToNormal2();
                    }
                }


            }
            //已上传
            else if (UPDATED == biaoshi) {
                if (c.isChecked()) {//未选中
                    UPDATEDNUM--;
                    if (UPDATEDNUM < Constants.itemUnmUnuped) {
                        mTbViewManager.setSelectAllButtonToNormal();
                    }
                } else {//选中等于list数目是改变颜色
                    UPDATEDNUM++;
                    if (UPDATEDNUM == Constants.itemUnmUnuped) {
                        mTbViewManager.setSelectAllButtonToNormal2();
                    }
                }
                Log.i("qweqwe", "asdas");

            }
        } else {
            Toast.makeText(mContext, "C NULL", Toast.LENGTH_SHORT).show();
        }
    }

    //在默认界面点击条目进来之后、 未上传、上传中、已上传 三个界面的点击条目的 点击方法 都在这个里面
    OnItemClickListener mItemClickListener = new OnItemClickListener() {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Log.d(TAG, "just clicked mItemClickListener");
            int OpType = mTbViewManager.getOpType();
            int UserType = mTbViewManager.getUserType();
            DataManager mDataManager = DataManager.getInstance(mContext);
            List<BXFile> allFiles = mTbViewManager.getAllFiles();
            Log.d(TAG, "onItemClick is start UserType is ..." + UserType
                    + " OpType is " + OpType);
            // TODO Auto-generated method stub
            CheckBox fileCheckBox = (CheckBox) view
                    .findViewById(R.id.fileCheckBox);

            List<BXFile> updatedata = null;
            if (UserType == UNUPDATED) {  //如果usertype是未上传的方式

                checkboxChannged(UserType, fileCheckBox);

                updatedata = mDataManager.getUnupdate(OpType); //List<BXFile> updatedata就是mDataManager下得到没有上传的数据
            } else if (UserType == UPDATING) {
                checkboxChannged(UserType, fileCheckBox);
                updatedata = mDataManager.getUpdating(OpType);
            } else if (UserType == UPDATED) {

                checkboxChannged(UserType, fileCheckBox);
                updatedata = mDataManager.getUpdated(OpType);
            }

//			System.out.println("单选的时候updatedata.size=="+updatedata.size());
//			System.out.println("单选的时候allFiles.size=="+allFiles.size());


            // View checkView = view.findViewById(R.id.checkView);
            BXFile bxfile = null;
            if ((OpType == BXFile.LOCAL_MODE) && (UserType == UNUPDATED)) {  //如果是手动上传并且还是未上传
                bxfile = allFiles.get(position);
                Log.d(TAG, "bxfile=allFiles.get(position)");
            } else {  //从默认目录 点进来 展示在 未上传下的 走这个
                bxfile = updatedata.get(position);
                Log.d(TAG, "bxfile=updatedata.get(position)");
            }
            if (bxfile.isDir()) { // 本地模式，点击文件夹
                mTbViewManager.InitLocalData(bxfile.getFilePath()); //如果是文件夹，就继续进去
            } else {
                if (!bxfile.isUsefulType()) { // 如果是不是允许上传的文件，则不允许添加
                    Utils.showShortMsg(R.string.errType);
                    return;
                } else if (mDataManager.checkLocalFileState(bxfile)) { // 如果文件在上传中或者是已上传，则不添加
                    // return;
                }
                List<BXFile> choosedFiles = BXFileManager.getInstance()
                        .getChoosedFiles();

                if (choosedFiles.contains(bxfile)) { //如果List<BXFile> choosedFiles中包含了选中的文件


                    choosedFiles.remove(bxfile);//就把他去掉，因为是用户取消勾选了
                    if (fileCheckBox != null)
                        fileCheckBox.setChecked(false);
                } else {
                    choosedFiles.add(bxfile); //如果List<BXFile> choosedFiles中没有包含选中的文件就把他添加进来

                    if (fileCheckBox != null)
                        fileCheckBox.setChecked(true);

                }
                Log.d(TAG, "choosed files is " + choosedFiles.size());
            }
        }
    };


}
