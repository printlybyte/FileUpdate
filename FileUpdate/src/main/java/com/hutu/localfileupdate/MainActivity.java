package com.hutu.localfileupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hutu.localfile.DefaultPage;
import com.hutu.localfile.LocalFileOp;
import com.hutu.localfile.SettingFra;
import com.hutu.localfile.manager.DataManager;
import com.hutu.localfile.manager.ListenerManager;
import com.hutu.localfile.manager.TbViewManager;
import com.hutu.localfile.util.ChosePreference;
import com.hutu.localfile.util.Utils;
import com.hutu.net.ConnectionChangeReceiver;
import com.hutu.zhang.CrashHandler;
import com.hutu.zhang.MyFileManager;
import com.hutu.zhang.RegUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class MainActivity extends FragmentActivity implements OnClickListener {
    public static final int FILE_RESULT_CODE = 1;
    private Button mBt1, mBt2, mBt4;   //最上面三个按钮 默认、本地、设置
    private static Button backButton;
    private ImageView mSelBg;
    private LinearLayout mTab_item_container;
    private FragmentManager mFM = null;
    public static Context mContext = null;
    private String TAG = "MainActivity";
    public static boolean isNotReg = true;
    LinearLayout content_container1, content_container2, content_container3,
            content_container4;  //四个页面
    private Fragment mDefaultOpFragment, mLocalFileFragment, mRoamingFragment;  //默认、本地、设置下的三个fragment
    private SettingFra mSettingFragment;
    static public String IMEI;
    private static final int VERSION_MSG = 1;
    private static final int SHOW_PASSWORD_INPUT_DIALOG = 2;
    private static final String NAMESPACE = "http://guanli.cha365.cn/WebServiceNewVersion.asmx";
    Intent m_Intent;
    //初始化网络监听广播
    private ConnectionChangeReceiver myReceiver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
//                    SharedPreferences ServerSetting = getSharedPreferences(
//                            "ServerSetting", 0);
//                    SharedPreferences.Editor editor = ServerSetting.edit();
//                    editor.putString("ftpIp", "218.246.35.197");
//                    editor.putString("ftpPort", "21");
//                    editor.putString("databaseIp", "kc.xun365.net");
//                    editor.putString("databasePort", "80");
//                    editor.commit();
                    DataManager.getInstance(MainActivity.this).getSupportData();
//                    init();
//                    defaultOption();

//                    registerBoradcastReceiver();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.title);  //自定义标题
        setContentView(R.layout.main);
        //初始化捕获异常的
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());


        initRegUtil();
        getIMEI();


        xxx();

    }

    private void xxx() {
        SharedPreferences isReged = getSharedPreferences(
                "isReged", 0);
        Boolean Reged = isReged.getBoolean("isReged", false); //获取到是否成功验证码通过的状态
        SharedPreferences ServerSetting = getSharedPreferences(
                "ServerSetting", 0);
        SharedPreferences.Editor editor = ServerSetting.edit();
        editor.putString("ftpIp", "218.246.35.197");
        editor.putString("ftpPort", "21");
        editor.putString("databaseIp", "kc.xun365.net");

        editor.putString("databasePort", "80");
        editor.commit();
        Utils mUtils = new Utils();
        mContext = this;
        DataManager.getInstance(this).getSupportData();
        init();
        defaultOption();

        registerBoradcastReceiver();
//        registerReceiver();//注册监听网络状态变化的广播
        // 都不为空的情况下 保存一下
        // 都不为空的情况下 保存一下


    }



    private void initRegUtil() {
        RegUtil regUtil = new RegUtil(this);
        regUtil.SetDialogCancelCallBack(new RegUtil.DialogCancelInterface() {
            @Override
            public void ToFinishActivity() {
                finish();
            }

            @Override
            public void ToFinishActivity_pwd() {
                finish();
            }
        });
    }


    private void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("com.hutu.startLocalFile");
        // 娉ㄥ唽骞挎挱
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private void unregisterBoradcastReceiver() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            backButton.setVisibility(View.INVISIBLE);
            int mAction = intent.getIntExtra("Type", 1);
            if ((mAction == 1)) {
                System.out.println("接受到1111111111111111111111的广播");
//				startActivity(new Intent(MainActivity.this,defaultMulu.class));   //我开启了一个新的页面
                last = mTab_item_container.getChildAt(mSelectIndex);
                now = mTab_item_container.getChildAt(1);
//				startAnimation(last, now);
                mSelectIndex = 1;
//				localOption2();
//				localOption();   //点击浏览目录之后出现的界面都在这个函数里
                //下面是我自己开始操作的了
                Intent mintent = new Intent(MainActivity.this, MyFileManager.class);
                startActivityForResult(mintent, FILE_RESULT_CODE);

            } else if (mAction == 2) {
                last = mTab_item_container.getChildAt(mSelectIndex);
                now = mTab_item_container.getChildAt(0);
                startAnimation(last, now);
                mSelectIndex = 0;
                defaultOption();
            } else if (mAction == 3) {
                last = mTab_item_container.getChildAt(mSelectIndex);
                now = mTab_item_container.getChildAt(2);
                startAnimation(last, now);
                mSelectIndex = 0;
                SettingOption();
            }
        }
    };

    //尝试使用函数
    private void ClickOverConfirmButton() {
        Bundle bundle = null;
//		if(data!=null&&(bundle=data.getExtras())!=null){
//			textView.setText("选中的路径= "+bundle.getString("file"));  


//			int i = Integer.parseInt(Utils.getDefautDirOp());
        //我把Utils.getDefautDirOp()里面的值弄出来了，转换成int类型了就可以显示是1,2,3,4,5中的一个了 设置显示目录的
        ChosePreference.updateEditView(Integer.parseInt(Utils.getDefautDirOp()), bundle.getString("file"));

        switch (Integer.parseInt(Utils.getDefautDirOp())) {
            case 1:
                Utils.setPreferences(Utils.DefaultPath[0], bundle.getString("file"));

                break;
            case 2:
                Utils.setPreferences(Utils.DefaultPath[1], bundle.getString("file"));
                break;
            case 3:
                Utils.setPreferences(Utils.DefaultPath[2], bundle.getString("file"));
                break;
            case 4:
                Utils.setPreferences(Utils.DefaultPath[3], bundle.getString("file"));
                break;
            case 5:
                Utils.setPreferences(Utils.DefaultPath[4], bundle.getString("file"));
                break;

            default:
                break;
//			}

        }
    }


    //在这个里面保存按键值和响应的目录？
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (FILE_RESULT_CODE == requestCode) {

            Bundle bundle = null;
            if (data != null && (bundle = data.getExtras()) != null) {
//				textView.setText("选中的路径= "+bundle.getString("file"));  
                System.out.println("在弹出设置默认目录的界面点击了 确认 键");
//                Toast.makeText(mContext, "点击了确定", Toast.LENGTH_SHORT).show();
//				尝试速度快的更新界面
//				String mDirString = Utils.getDefautDirOp();
//				int index = Integer.parseInt(mDirString); 
////				DataManager.InitSupportData(index-1);
//				DataManager mDataManager = DataManager.getInstance(MainActivity.mContext);
//				mDataManager.refreshAutoPathData(index - 1);

                DataManager.getInstance(this).getSupportData();
                DefaultPage.updateViewInfo();

                //发送重新初始化  解决不能设置完毕后还得退出后重启才能上传
               mHandler.sendEmptyMessage(11);
//				TbViewManager.updateViewTitleInfo(TbViewManager.mOpreType,
//						TbViewManager.currentView, false);

//				int i = Integer.parseInt(Utils.getDefautDirOp());
                //我把Utils.getDefautDirOp()里面的值弄出来了，转换成int类型了就可以显示是1,2,3,4,5中的一个了 设置显示目录的
                ChosePreference.updateEditView(Integer.parseInt(Utils.getDefautDirOp()), bundle.getString("file"));

                switch (Integer.parseInt(Utils.getDefautDirOp())) {
                    case 1:
                        Utils.setPreferences(Utils.DefaultPath[0], bundle.getString("file"));

                        break;
                    case 2:
                        Utils.setPreferences(Utils.DefaultPath[1], bundle.getString("file"));
                        break;
                    case 3:
                        Utils.setPreferences(Utils.DefaultPath[2], bundle.getString("file"));
                        break;
                    case 4:
                        Utils.setPreferences(Utils.DefaultPath[3], bundle.getString("file"));
                        break;
                    case 5:
                        Utils.setPreferences(Utils.DefaultPath[4], bundle.getString("file"));
                        break;

                    default:
                        break;
                }

            }
        }
    }

    private void init() {
        mTab_item_container = (LinearLayout) findViewById(R.id.tab_item_container);


        mBt1 = (Button) findViewById(R.id.tab_bt_1);
        mBt2 = (Button) findViewById(R.id.tab_bt_2);
        mBt4 = (Button) findViewById(R.id.tab_bt_4);
        backButton = (Button) findViewById(R.id.TitleBackBtn);

        mBt1.setOnClickListener(this);
        mBt2.setOnClickListener(this);
        mBt4.setOnClickListener(this);


        mSelBg = (ImageView) findViewById(R.id.tab_bg_view);
        content_container1 = (LinearLayout) findViewById(R.id.content_container1);
        content_container2 = (LinearLayout) findViewById(R.id.content_container2);
        content_container3 = (LinearLayout) findViewById(R.id.content_container3);
        content_container4 = (LinearLayout) findViewById(R.id.content_container4);

        mDefaultOpFragment = new DefaultPage(this);
        mLocalFileFragment = new LocalFileOp(this); //操作localfile的一个类
        mSettingFragment = new SettingFra(this);

        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d(TAG, "mSelectIndex is " + mSelectIndex);
                switch (mSelectIndex) {
                    case 0:
                        backButton.setVisibility(View.GONE);
                        defaultOption();
                        DefaultPage.backShow();
                        break;
                    case 1:

                        break;

                    default:
                        break;
                }
            }
        });
    }

    public static void SetBackTileShow() {
        backButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        LayoutParams lp = mSelBg.getLayoutParams();
        lp.width = mTab_item_container.getWidth() / 3;
    }

    private int mSelectIndex = 0;
    private View last, now;
    View v1, v2;

    @Override
    public void onClick(View arg0) {
        backButton.setVisibility(View.INVISIBLE);
        switch (arg0.getId()) {
            case R.id.tab_bt_1:  //默认按钮
                TbViewManager.restoreSelectAllButton();
                last = mTab_item_container.getChildAt(mSelectIndex);
                now = mTab_item_container.getChildAt(0);
                startAnimation(last, now);
                mSelectIndex = 0;

                defaultOption();
                ListenerManager.isLocalButtonClicked = false;
                TbViewManager.restoreSelectAllButton();
                TbViewManager.defalutPageItemOrder = 10;

                break;
            case R.id.tab_bt_2: //本地
                ListenerManager.buttonID = 1;  //点本地的时候这个值设置为1
                last = mTab_item_container.getChildAt(mSelectIndex);
                now = mTab_item_container.getChildAt(1);
                startAnimation(last, now);
                mSelectIndex = 1;
                localOption();
                ListenerManager.isLocalButtonClicked = true;
                break;
            case R.id.tab_bt_4: //设置
                last = mTab_item_container.getChildAt(mSelectIndex);
                now = mTab_item_container.getChildAt(2);
                startAnimation(last, now);
                mSelectIndex = 2;

                SettingOption();
                break;
            default:
                break;
        }
    }

    /**
     * 这个方法就是图标上面蓝色的点动画的执行
     *
     * @param last
     * @param now
     */
    private void startAnimation(View last, View now) {
        TranslateAnimation ta = new TranslateAnimation(last.getLeft(),
                now.getLeft(), 0, 0);
        ta.setDuration(300);
        ta.setFillAfter(true);
        mSelBg.startAnimation(ta);
    }

    /**
     * 榛樿椤甸潰
     */
    public void defaultOption() {
        content_container1.setVisibility(View.VISIBLE);
        content_container2.setVisibility(View.GONE);
        content_container3.setVisibility(View.GONE);
        content_container4.setVisibility(View.GONE);

        if (null == mFM)
            mFM = getSupportFragmentManager();

        FragmentTransaction ft = mFM.beginTransaction();


        Utils.cleanProperties();//切换就取消设定默认
        if ((mDefaultOpFragment.isAdded())
                && (!mDefaultOpFragment.isRemoving())) {
            ft.hide(mLocalFileFragment).show(mDefaultOpFragment).commit();
        } else {
            ft.hide(mLocalFileFragment)
                    .add(R.id.content_container1, mDefaultOpFragment,
                            "mDefaultOpFragment").show(mDefaultOpFragment)
                    .commit();
        }
    }

    /**
     * 鏈湴椤甸潰  点击本地之后出现的操作
     * 点击浏览目录之后出现的界面都在这个函数里
     */
    public void localOption() {


        content_container1.setVisibility(View.GONE);
        content_container2.setVisibility(View.VISIBLE);   //使只显示第二个LinearLayout
        content_container3.setVisibility(View.GONE);
        content_container4.setVisibility(View.GONE);

        if (null == mFM)
            mFM = getSupportFragmentManager();   //获取一个fragmentManager

        FragmentTransaction ft = mFM.beginTransaction();  //可以这样得到FragmentTransaction类的实例：　
        //这个if语句是判断是否加载过mLocalFileFragment，如果没有加载过就加载，要是加载了就重新显示
        //尝试1，mLocalFileFragment改成另外一个东西
        if (mLocalFileFragment.isAdded()) {
            ft.hide(mDefaultOpFragment).show(mLocalFileFragment).commit();
        } else {
            ft.hide(mDefaultOpFragment)
                    .add(R.id.content_container2, mLocalFileFragment)
                    .show(mLocalFileFragment).commit();
        }
    }

    /**
     * 我要在这个里面实现把上面（未上传，上传中，已上传）这个东西给去掉
     */
    private void localOption2() {
        // TODO Auto-generated method stub
        content_container1.setVisibility(View.GONE);
        content_container2.setVisibility(View.VISIBLE);
        content_container3.setVisibility(View.GONE);
        content_container4.setVisibility(View.GONE);
        if (null == mFM)
            mFM = getSupportFragmentManager();   //获取一个fragmentManager

        FragmentTransaction ft = mFM.beginTransaction();  //可以这样得到FragmentTransaction类的实例：　
        ft.hide(mDefaultOpFragment)
                .add(R.id.content_container2, mLocalFileFragment)
                .show(mLocalFileFragment).commit();

    }


    /**
     * 璁剧疆椤甸潰
     * 设置界面
     */
    public void SettingOption() {
        // SettingFra f = new SettingFra(this);

        Utils.cleanProperties(); //切换就取消设定默认
        content_container1.setVisibility(View.GONE);
        content_container2.setVisibility(View.GONE);
        content_container3.setVisibility(View.VISIBLE);
        content_container4.setVisibility(View.GONE);

        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction transaction = fragmentManager
                .beginTransaction();

        if (mSettingFragment.isAdded()) {
            if (null == mFM)
                mFM = getSupportFragmentManager();
            FragmentTransaction ft = mFM.beginTransaction();
            ft.hide(mDefaultOpFragment).hide(mLocalFileFragment).commit();

            transaction.show(mSettingFragment).commit();
        } else {
            transaction.add(R.id.content_container3, mSettingFragment).commit();
        }
    }

    @Override
    public void onDestroy() {
        unregisterBoradcastReceiver();
//        unregisterReceiver();
        Log.i("qweqwe", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Log.d(TAG, "妯睆妯″紡");
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Log.d(TAG, "绔栧睆妯″紡");
        }
    }

    /**
     * 检测版本是否可用
     *
     * @param version  当前软件版本号
     * @param type     1PC 2android 3IOS
     * @param appCode  软件唯一标识码值
     * @param num      注册码
     * @param onlyCode IMEI
     * @return 0当前已为最新
     * 1 版本号:v1.0$软件大小:2.3M$更新日期:2012-10-22 14:23:59$更新功能:1.增加提醒提示;2.优化登录显示速度;3.优化软件网络流量使用;$下载地址:http://jat.beidoustar.com/softs/内测版/家安通/JAT_Alpha_MB_CN_V1.7.apk
     * 2 最新版本获取失败,请联系管理员
     */
    public String VerifUse(String version, int type, String appCode, String num, String onlyCode) {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap12:Body>"
                + "<NewVersion  xmlns=\"http://tempuri.org/\">"
                + "<appCode>"
                + appCode
                + "</appCode>"
                + "<version>"
                + version
                + "</version>"
                + "<type>"
                + type
                + "</type>"
                + "<number>"
                + num
                + "</number>"
                + "<onlyCode>"
                + onlyCode
                + "</onlyCode>"
                + "</NewVersion>" + "</soap12:Body>" + "</soap12:Envelope>";
        Log.e("sendXml", xml);
        // 不等于0就成功
        return getWebServiceResponse(xml, "NewVersionResult", NAMESPACE);
    }

    /**
     * 调用webservice接口
     *
     * @param xml       发送的xml
     * @param backitem  要返回的数据字段
     * @param namespace 访问的地址
     * @return
     */
    public String getWebServiceResponse(String xml, String backitem,
                                        String namespace) {
        String res = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(namespace);
        try {
            HttpEntity re = new StringEntity(xml, HTTP.UTF_8);
            httppost.setHeader("Content-Type",
                    "application/soap+xml; charset=utf-8");
            httppost.setEntity(re);
            HttpResponse response = httpClient.execute(httppost);
            try {
                res = parseResponseXML(
                        new ByteArrayInputStream(
                                EntityUtils.toByteArray(response.getEntity())),
                        backitem);
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.e(backitem, "webService=" + res);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return res;
    }

    /**
     * 解析返回xml
     *
     * @param inStream  解析xml流
     * @param returnStr 要匹配的的解析字段
     * @return
     * @throws Exception
     */

    public String parseResponseXML(InputStream inStream, String returnStr)
            throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inStream, "UTF-8");
        int eventType = parser.getEventType();// 产生第一个事件
        while (eventType != XmlPullParser.END_DOCUMENT) {// 只要不是文档结束事件
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();// 获取解析器当前指向的元素的名称
                    if (returnStr.equals(name)) {
                        return parser.nextText();
                    }
                    break;
            }
            eventType = parser.next();
        }
        return null;
    }

    private void getIMEI() {
        TelephonyManager telephonyManager = (TelephonyManager) MainActivity.this
                .getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
    }

    /*
     * 获取当前程序的版本号
     */
    public static String getVersionName(Context context) throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);
        return packInfo.versionName;
    }


    //再按一次退出
    private long exitTime = 0;

    /**
     * 捕捉返回事件按钮
     * <p>
     * 因为此 Activity 继承 TabActivity 用 onKeyDown 无响应，所以改用 dispatchKeyEvent
     * 一般的 Activity 用 onKeyDown 就可以了
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                this.exitApp();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 退出程序
     */
    private void exitApp() {
        // 判断2次点击事件时间
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
