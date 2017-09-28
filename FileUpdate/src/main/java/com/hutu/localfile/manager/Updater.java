/**
 * @Title: Updater.java
 * @Package com.hutu.localfile.manager
 * @Description: TODO
 * @author Long Li
 * @date 2015-5-6 下午4:38:05
 * @version V1.0
 */
package com.hutu.localfile.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.hutu.databases.DbFile;
import com.hutu.ftputils.ContinueFtp;
import com.hutu.ftputils.ContinueFtp.FtpState;
import com.hutu.ftputils.ContinueFtp.UploadStatus;
import com.hutu.localfileupdate.R;

import java.io.IOException;

/**
 * TODO<上传器>
 *
 * @author Long Li
 * @data: 2015-5-6 下午4:38:05
 * @version: V1.0
 */
public class Updater {

    private String TAG = "longli";
    private ContinueFtp cFtp;

    private String FtpHostAdress = null;

    // private String FtpUserName = "FTPtest";
    // private String FtpUserPwd = "Ftp112233";
    private String FtpUserName = "FTPuser"; // FTP 用户名 密码
    private String FtpUserPwd = "Ftp1029384756";
    // private String FtpRmtPath = "/FTPUpload/test/";
    // for release
    private int FtpHostPort = 21; // 端口
    private String FtpRmtPath = "/web/KuaiChuan/web/Upload/"; // 服务器端路径

    public Updater(Context mContext, BXFile mFile, Handler mHandler,
                   DbFile mDbFile) {
        this.cFtp = new ContinueFtp(mContext, mHandler, mDbFile, mFile);
        FtpHostAdress = mContext.getString(R.string.ServerIp);
        // SharedPreferences prefs =
        // PreferenceManager.getDefaultSharedPreferences(mContext);
        // FtpHostAdress = prefs.getString("ServerIp", FtpHostAdress);

        // 使用上帝从SharedPreferences里面得到databaseIp
        SharedPreferences ServerSetting = LFBApplication.getApplication()
                .getSharedPreferences("ServerSetting", 0);
        FtpHostAdress = ServerSetting.getString("ftpIp", FtpHostAdress);

        Log.d("IPandPort", FtpHostAdress);
    }

    // 开始上传一个文件
    public void StartUpdate() {
        if (cFtp.getFtpState() == FtpState.UPDATING) {
            Log.d(TAG, "StartUpDate ftp state is updating");
            return;
        }

        // 使用上帝从SharedPreferences里面得到databasePort
        SharedPreferences ServerSetting = LFBApplication.getApplication()
                .getSharedPreferences("ServerSetting", 0);
        FtpHostPort = Integer.valueOf(ServerSetting.getString("ftpPort", "21"));

        Log.d("IPandPort", String.valueOf(FtpHostPort));

        cFtp.setFtpState(FtpState.UPDATING);
        Log.d(TAG, "cft connect IP is " + FtpHostAdress);
        cFtp.SetConnectInfos(FtpHostAdress, FtpHostPort, FtpUserName,
                FtpUserPwd, FtpRmtPath);
//		Log.i("qweqwe",""+FtpHostAdress+" = "+FtpHostPort+" = "+FtpUserName+" = "+FtpUserPwd+" = "+FtpRmtPath);
        try {

            boolean isConnect = cFtp.connect();

            if (isConnect) {
                UploadStatus mStatus = cFtp.upload();
                Log.i("qweqwea", "ftp result status is asdasdasdasdasdasdasdasa" + mStatus);
                if ((UploadStatus.Upload_New_File_Success == mStatus) // 上传成功
                        || (UploadStatus.Upload_From_Break_Success == mStatus)) {
                    cFtp.sendMsg(100);
                    Log.i("qweqwea", "上传成功 " + mStatus);
                } else if ((UploadStatus.Upload_From_Break_Failed == mStatus) // 上传失败
                        || (UploadStatus.Upload_New_File_Failed == mStatus)) {
                    cFtp.setFtpState(FtpState.INIT);
                    Log.i("qweqwea", "上传失败 " + mStatus);
                } else if (UploadStatus.File_Exits == mStatus) { // 移除文件到已上传列表
                    cFtp.sendMsg(100);
                } else {
                    Log.i("qweqwea", "status is " + mStatus);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            // pause(); //pase state
        } catch (NullPointerException e) {
            // TODO: handle exception
            e.printStackTrace();
            // pause();
        } finally {
            cFtp.setFtpState(FtpState.INIT);
            try {
                cFtp.disconnect();
                Log.i("qweqweqwe","连接断开2");
                pause();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    public void sendBroadCastnetStart(Context mContext, int type) {
        Intent mIntent = new Intent("netstart.listion");
        mIntent.putExtra("Type", type);
        // 发送广播
        mContext.sendBroadcast(mIntent);
    }
    //
    public boolean isupdating() {
        // return FtpState.UPDATING == mState;
        return FtpState.UPDATING == cFtp.getFtpState();
    }

    // 暂停上传
    public void pause() {
        cFtp.pause();
        try {
            cFtp.disconnect();
        } catch (Exception e) { // IOException
            // TODO Auto-generated catch block
            System.out.println("暂停失败");
            e.printStackTrace();

        }
    }
}
