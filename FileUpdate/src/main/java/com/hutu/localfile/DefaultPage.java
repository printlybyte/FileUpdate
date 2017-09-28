package com.hutu.localfile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutu.databases.DbFile;
import com.hutu.localfile.manager.BXFile;
import com.hutu.localfile.manager.BXFileManager;
import com.hutu.localfile.manager.DataManager;
import com.hutu.localfile.manager.ListenerManager;
import com.hutu.localfile.manager.LocalDefaultFile;
import com.hutu.localfile.manager.TbViewManager;
import com.hutu.localfile.manager.UpdateManager;
import com.hutu.localfile.util.Utils;
import com.hutu.localfileupdate.MainActivity;
import com.hutu.localfileupdate.R;
import com.hutu.net.NetworkManager;
import com.hutu.zhang.Constantx;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPage extends Fragment {

    private Context mContext = null;
    private BXFileManager bfm;
    private String TAG = "DefaultPage";
    private FragmentManager mFM = null;
    private Fragment Fragment1;
    LinearLayout content_one, content_two, content_three, content_four,
            content_five;
    private TbViewManager mTbViewManager = null;
    private static CheckBox mBox1, mBox2, mBox3, mBox4, mBox5;
    private static TextView DirText1, DirText2, DirText3, DirText4, DirText5;
    private static TextView numText1, numText2, numText3, numText4, numText5;

    private LinearLayout[] mLinearLayouts = {content_one, content_two,
            content_three, content_four, content_five};

    // 这里把显示文件夹名 文件个数 还有勾选框 linearlayout 都弄成了集合

    private static TextView[] mNumTexts = {numText1, numText2, numText3,
            numText4, numText5};
    private static TextView[] mDrEditTexts = {DirText1, DirText2, DirText3,
            DirText4, DirText5};
    private static CheckBox[] mCheckBoxs = {mBox1, mBox2, mBox3, mBox4, mBox5};

    private int[] mlayoutIds = {R.id.default_one, R.id.default_two,
            R.id.default_three, R.id.default_four, R.id.default_five};

    private Map<Integer, Integer> button_ids = new HashMap<Integer, Integer>();

    private int[] mFileboxIds = {R.id.fileCheckBox_one, R.id.fileCheckBox_two,
            R.id.fileCheckBox_three, R.id.fileCheckBox_four,
            R.id.fileCheckBox_five};

    private int[] mDTextIds = {R.id.dirname_one, R.id.dirname_two,
            R.id.dirname_three, R.id.dirname_four, R.id.dirname_five};

    private int[] mNTextIds = {R.id.num_one, R.id.num_two, R.id.num_three,
            R.id.num_four, R.id.num_five};

    public static int[] default_ids = {R.string.default_one,
            R.string.default_two, R.string.default_three,
            R.string.default_four, R.string.default_five};


    static TextView t1;
    // private String AUTO_PATH = "auto_path";
    // private String AUTO_FILE = "auto_file";
    // private String speString = "A&A";
    private static View mView;

    private Handler mAutoHander = new Handler() { // 每隔2分钟自动检测文件夹数据自动上传
        @Override
        public void handleMessage(Message msg) {
            if (1 == msg.what) {
                TbViewManager.updateViewTitleInfo(TbViewManager.mOpreType,
                        TbViewManager.currentView, false);
                StartUpdateFile();
            }
            super.handleMessage(msg);
        }
    };

    public DefaultPage(Context context) {
        mContext = context;
        for (int i = 0; i < BXFile.LOCAL_MODE; i++) {
            button_ids.put(mlayoutIds[i], i);
        }
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("com.hutu.networkChange");
        // 注册广播
        mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    public void unregisterBoradcastReceiver() {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    private static int curAction = 0;
    private static boolean autRuning = false;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int mAction = intent.getIntExtra("Type", 0);
            if ((mAction != curAction) || (curAction == 3)) {
                curAction = mAction;

                switch (mAction) {
                    case 1:
                    case 2:

//                        Constantx.isWork = 2;
//                        sendBroadCastnetStart(mContext,1);
                        if (!autRuning) {
                            Log.d("qweqwea", "network is on");
                            StartUpdateFile();
                        }


                        break;

                    case 3: // 数据上传完毕
                        Log.d(TAG, "Data fresh over");
                        // 更新状态栏
                        updateViewInfo();
                        TbViewManager.updateViewTitleInfo(TbViewManager.mOpreType,
                                TbViewManager.currentView, false);
//                        if (!autRuning)
//                            StartUpdateFile();
                        break;

                    case 0:
//                        Utils.setPreferences("file_setting", "2");
                        DataManager mDataManager = DataManager.getInstance(mContext);
                        UpdateManager mUpdateManager = UpdateManager.getInstance(
                                TbViewManager.mUpdatingHander, mContext);
                        List<BXFile> updatingFiles = mDataManager
                                .getUpdating(BXFile.LOCAL_MODE); // 获取所有的上传中文件
                        String A;
                        String sp, sp2 = null;
                        //更换上传中的名字   删除本地数据库信息  路径才能获取到文件
                        for (int i = 0; i < updatingFiles.size(); i++) {
                            String formatVideo[] = updatingFiles.get(i).getFilePath().toString().split("\\.");
                            String formatVideo2 = formatVideo[formatVideo.length - 1];
                            A = (updatingFiles.get(i).getFilePath() + "." + formatVideo2).toString();
                            String resplit[] = A.split("\\.");
                            Log.d(TAG, "拆分的大小是" + resplit.length + "");
                            //整合A.de 数据以及路径
                            for (int i1 = 0; i1 < resplit.length; i1++) {
                                Log.i(TAG, "拆分出来的数据是\n" + resplit[i1]);
                                if (resplit.length > 2) {
                                    java.util.Random r = new java.util.Random();
                                    sp = resplit[0];
                                    String splitArr[] = sp.split("/");
                                    Log.i(TAG, "第二次拆分的大小是  ：" + splitArr.length);
                                    StringBuilder sb = new StringBuilder();
                                    //对路径（包含名字进行数据整理   采用随机数 防止不重复）
                                    for (int i2 = 0; i2 < splitArr.length; i2++) {

                                        if (i2 == splitArr.length - 1) {
                                            sp2 = splitArr[splitArr.length - 1];
                                            if (sp2.length() > 15) {
                                                sp2 = sp2.substring(0, 15);
                                                sb.append("/" + sp2);
                                            } else {
                                                sb.append("/" + sp2);
                                            }
                                        } else {
                                            //等于0为/不需要
                                            if (i2 != 0) {
                                                sb.append("/" + splitArr[i2]);
                                            }
                                        }
                                    }
                                    Log.i("qweqweas", "sb== " + sb.toString() + "sp2" + sp2);
                                    A = sb + String.valueOf(r.nextInt()).substring(3, 6) + "." + resplit[resplit.length - 1];
                                    Log.i("qweqweas", "A== " + A);
                                }
                            }
                            renameFile(updatingFiles.get(i).getFilePath(), A);//更改名字

                            DbFile.getDbInstance(mContext).deletInfo(updatingFiles.get(i).getFileName(), updatingFiles.get(i).getFilePath());
                        }
                        updateViewInfo();
                        TbViewManager.updateViewTitleInfo(TbViewManager.mOpreType,
                                TbViewManager.currentView, false);
                        DataManager.getInstance(mContext).getSupportData();
                        Log.d(TAG, "no network");

                        break;
                    default:

                        Log.d(TAG, "no network");
                        break;
                }
            }
        }

    };

    public boolean renameFile(String file, String toFile) {

        File toBeRenamed = new File(file);
        Log.d(TAG, "文件路径: " + file + toFile);
        // 检查要重命名的文件是否存在，是否是文件
        if (!toBeRenamed.exists() || toBeRenamed.isDirectory()) {

            Log.d(TAG, "文件不存在: " + file);
            return false;
        }

        File newFile = new File(toFile);

        // 修改文件名
        if (toBeRenamed.renameTo(newFile)) {
            Log.d(TAG, "重命名成功.名字是" + toBeRenamed.getName() + "路径是：" + toBeRenamed.getAbsolutePath());
            return true;
        } else {
            Log.d(TAG, "重命名失败");
            return false;
        }

    }

    private void StartUpdateFile() {
        autRuning = true;
        NetworkManager mNetworkManager = new NetworkManager(mContext);

        if (!mNetworkManager.CheckNetworkPermisson()) {
            Message message = Message.obtain();
            message.what = 9;
            message.obj = mContext;
            TbViewManager.mUpdatingHander.sendMessageDelayed(message, 3000);
            autRuning = false;
            return;
        }

        DataManager mDataManager = DataManager.getInstance(mContext);
        UpdateManager mUpdateManager = UpdateManager.getInstance(
                TbViewManager.mUpdatingHander, mContext);
        List<BXFile> updatingFiles = mDataManager
                .getUpdating(BXFile.LOCAL_MODE); // 获取所有的未上传文件
        if (updatingFiles.size() != 0) {
            if (mUpdateManager != null) {
                Log.d("qweqwea", "开始自动上传未上传的文件");
                mUpdateManager.BatchUpdate(updatingFiles);
            }
        }

        autRuning = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bfm = BXFileManager.getInstance();
        registerBoradcastReceiver();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.default_page, container, false);
        t1 = (TextView) v.findViewById(R.id.num_one);
        init(v);
        updateViewInfo();
        return v;
    }

    private boolean sscanAutoPath() {
        boolean result = false;


		/*
         * String mstate = Utils .getPreferences(Utils.AutoMode); if ((mstate ==
		 * null) || (mstate.length() == 0)) { return result; }
		 */

        for (int i = 0; i < Utils.DEFAULT_NUMS; i++) {
            // 判断是否开启自动上传功能
            String mstate = Utils
                    .getPreferences(Utils.DefaultPath[i] + Utils.A);
            if ((mstate != null) && (mstate.length() > 0)) { // 已经设定
                DataManager mDataManager = DataManager.getInstance(mContext);
                mDataManager.ScanSupportData(i);
                // 检查是否有未上传文件，有的话，设置成上传中状态，并且返回true
                // fixme:数据比较大时需要妥善处理

                if (mDataManager.changeUnupDatasState(i)) {
                    result = true;
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = i;
                    mAutoHander.sendMessage(msg);
                }
            }

        }

        return result;
    }

    private boolean start = false;

    class myThread extends Thread {
        public void run() {
            boolean result = false;
            start = true;
            DataManager mDataManager = DataManager.getInstance(mContext);
            while (!mDataManager.getRefreshDataResult()) { // 数据没有更新完不允许自动扫描和上传
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            while (start) {
                result = false;
                if (sscanAutoPath()) {
                    result = true;

                }

                Log.d(TAG, "sscan result is " + result);
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        Log.d(TAG, "onResume");
        if (!start) {
            new myThread().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBoradcastReceiver();
        start = false;
        mAutoHander = null;
        Log.d(TAG, "onDestroy");
    }

    /**
     * 默认界面 五个条目 的点击事件，点击进入可以看到响应的未上传的目录
     */
    OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {

            //点击默认进来
            // TODO Auto-generated method stub
            ListenerManager.buttonID = 0;  //要是从五个条目点击过去 就是0
            int action = 0;
            if (button_ids == null) {
                Log.d(TAG, "button_ids is null");
                return;
            }
            if (button_ids.get(arg0.getId()) != null) {
                action = button_ids.get(arg0.getId());
            }

            String mpath = Utils.getPreferences(Utils.DefaultPath[action]);
            if ((mpath != null) && (mpath.length() > 0)) {
                TbViewManager.defalutPageItemOrder = action;  //把具体从哪个目录进来的赋值给一个变量，从而在全选的时候不至于报错
                TbViewManager.tempOrder = action;
                showAction(action);

            } else {
                Utils.showSetDefDirDialog();
            }

        }
    };

    private void init(View v) {
        mView = v;

        Fragment1 = new LocalDefaultFile(mContext);

        for (int i = 0; i < Utils.DEFAULT_NUMS; i++) {
            mLinearLayouts[i] = (LinearLayout) mView
                    .findViewById(mlayoutIds[i]);
            mCheckBoxs[i] = (CheckBox) mView.findViewById(mFileboxIds[i]);
            mDrEditTexts[i] = (TextView) mView.findViewById(mDTextIds[i]);
            mNumTexts[i] = (TextView) mView.findViewById(mNTextIds[i]);
            mLinearLayouts[i].setOnClickListener(mClickListener); // 默认 界面 五个默认目录 上 设置 点击监听器
        }


    }

    public static int getlist(File f) {// 递归求取目录文件个数
        int size = 0;
        if (f == null) {
            return size;
        } else {
            File flist[] = f.listFiles();
            size = flist.length;
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {
                    size = size + getlist(flist[i]);
                    size--;
                }
            }
        }


        return size;
    }

    // 更新页面显示数据
    // 6.21从static改成public了
    public static void updateViewInfo() {
        Log.d("new快传终端DefaulfPage类", "执行了updateViewInfo方法");
//		int allsize=0;
        /*
         * String mstate = Utils .getPreferences(Utils.AutoMode);
		 */
        for (int i = 0; i < 5; i++) {
            // 已经在mainactivity里面把五个按键的值绑定了
            String mpath = Utils.getPreferences(Utils.DefaultPath[i]);
            System.out.println("Utils.DefaultPath[i]打印出来是---------" + Utils.DefaultPath[i]);
            System.out.println("mpath打印出来是------------------------" + mpath);

            if ((mpath != null) && (mpath.length() > 0)) {
                File mFile = new File(mpath);
                mDrEditTexts[i].setText(mFile.getName());
                Log.d("new快传终端DefaulfPage类", "设置完文件目录参数");
                DataManager mDataManager = DataManager
                        .getInstance(MainActivity.mContext);


//				allsize=getlist(new File(mpath));  //就他妈的这一行代码解决了数目更新不及时的问题

                //之前是这里得到总的个数
                int allsize = mDataManager.getUnupdate(i).size()
                        + mDataManager.getUpdating(i).size()
                        + mDataManager.getUpdated(i).size();

                Log.i("DefaultPage", "allsize is " + allsize + mDataManager.getUnupdate(i).size()
                        + mDataManager.getUpdating(i).size()
                        + mDataManager.getUpdated(i).size());
                mNumTexts[i].setText(String.format(
                        MainActivity.mContext.getString(R.string.default_num),
                        allsize));

                if (mNumTexts[i] == t1) {
                    Log.i("QWEQWE", allsize + "===");

                }

                Log.d("new快传终端DefaulfPage类", "设置完文件数量参数");
                System.out.println("执行设置文件夹数的代码已经执行完了"
                        + String.format(MainActivity.mContext
                        .getString(R.string.default_num), allsize));
                String mstate = Utils.getPreferences(Utils.DefaultPath[i]
                        + Utils.A);
                if ((mstate != null) && (mstate.length() > 0)) { // 已经设定
                    mCheckBoxs[i].setChecked(true);
                } else {
                    mCheckBoxs[i].setChecked(false);
                }
            } else {
                mDrEditTexts[i].setText(MainActivity.mContext
                        .getString(R.string.default_dirname));
                mNumTexts[i].setText(MainActivity.mContext
                        .getString(R.string.default_numtitle));

                mCheckBoxs[i].setChecked(false);
            }
        }
    }

    public void sendBroadCastToCenter(Context mContext, int type) {
        Intent mIntent = new Intent("com.hutu.startLocalFile");
        mIntent.putExtra("Type", type);
        // 发送广播
        mContext.sendBroadcast(mIntent);
    }

    public void sendBroadCastnetStart(Context mContext, int type) {
        Intent mIntent = new Intent("netstart.listion");
        mIntent.putExtra("Type", type);
        // 发送广播
        mContext.sendBroadcast(mIntent);
    }

    public static void backShow() {
        updateViewInfo();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden == false) {
            updateViewInfo();
        }
        super.onHiddenChanged(hidden);
    }

    /**
     * @param action 在默认界面 五个默认目录 点进来之后展示条目的方法
     */
    private void showAction(int action) {

        ((FragmentActivity) mContext).findViewById(R.id.content_container1)
                .setVisibility(View.GONE);
        ((FragmentActivity) mContext).findViewById(R.id.content_container2)
                .setVisibility(View.GONE);
        ((FragmentActivity) mContext).findViewById(R.id.content_container3)
                .setVisibility(View.GONE);
        ((FragmentActivity) mContext).findViewById(R.id.content_container4)
                .setVisibility(View.VISIBLE);

        MainActivity.SetBackTileShow();
        if (null == mFM)
            mFM = ((FragmentActivity) mContext).getSupportFragmentManager();

        FragmentTransaction ft = mFM.beginTransaction();

        if (Fragment1.isAdded()) {
            Log.d(TAG, "已经加载了Fragment1");
            ((LocalDefaultFile) Fragment1).setmAction(action);
            ((LocalDefaultFile) Fragment1).setOpType();
            LocalDefaultFile.refreshVideoView();
            ft.show(Fragment1).commit();

        } else {
            Log.d(TAG, "加载Fragment1");
            ft.add(R.id.content_container4, Fragment1, "Fragment1");
            ((LocalDefaultFile) Fragment1).setmAction(action);
            ft.show(Fragment1).commit();

        }

    }
}
