package com.hutu.ftputils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hutu.databases.DbFile;
import com.hutu.localfile.manager.BXFile;
import com.hutu.localfile.manager.BXFile.FileState;
import com.hutu.localfile.util.Utils;
import com.hutu.net.httpUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;

public class ContinueFtp {
    // 枚举类UploadStatus代码
    public enum UploadStatus {
        Create_Directory_Fail, // 远程服务器相应目录创建失败
        Create_Directory_Success, // 远程服务器闯将目录成功
        Upload_New_File_Success, // 上传新文件成功
        Upload_New_File_Failed, // 上传新文件失败
        File_Exits, // 文件已经存在
        Remote_Bigger_Local, // 远程文件大于本地文件
        Upload_From_Break_Success, // 断点续传成功
        Upload_From_Break_Failed, // 断点续传失败
        Delete_Remote_Faild; // 删除远程文件失败
    }

    // 枚举类DownloadStatus代码
    public enum DownloadStatus {
        Remote_File_Noexist, // 远程文件不存在
        Local_Bigger_Remote, // 本地文件大于远程文件
        Download_From_Break_Success, // 断点下载文件成功
        Download_From_Break_Failed, // 断点下载文件失败
        Download_New_Success, // 全新下载文件成功
        Download_New_Failed; // 全新下载文件失败
    }

    public enum FtpState {
        INIT, // 初始化 0
        PAUSE, // 暂停 1
        UPDATING, // 上传中 2
    }

    public FTPClient ftpClient = new FTPClient();
    private String ftphost, ftpuname, ftppwd;
    int ftpport;
    private String remotePath;
    private Context context;
    private Handler handler;
    public static final int HANDLER_FTP_UPLOAD = 110; // 上传文件
    private DbFile mDbFile;
    private BXFile mBxFile;
    private String TAG = "longli";
    private FtpState mState = FtpState.INIT;
    private int PROGRESS = 10;
    // FTP协议里面，规定文件名编码为iso-8859-1
    private String LOCAL_CHARSET = "GBK";
    private String SERVER_CHARSET = "ISO-8859-1";

    public void pause() {
        mState = FtpState.PAUSE;
    }

    public FtpState getFtpState() {
        return mState;
    }

    public void setFtpState(FtpState mState) {
        this.mState = mState;
    }

    public ContinueFtp() {
    }

    public ContinueFtp(Context context, Handler handler, DbFile mDbFile,
                       BXFile mBxFile) {
        this.context = context;
        this.handler = handler;
        this.mDbFile = mDbFile;
        this.mBxFile = mBxFile;
    }

    public void SetConnectInfos(String hostname, int port, String username,
                                String password, String remotePath) {
        this.ftphost = hostname;
        this.ftpuname = username;
        this.ftpport = port;
        this.ftppwd = password;
        this.remotePath = remotePath;
    }

    public String getRemotePath() {
        return this.remotePath;
    }

    /**
     * @return
     * @throws IOException
     * @连接到Ftp
     */
    public boolean connect() throws IOException {
        // String LOCAL_CHARSET;

        // ftpClient.setControlEncoding("utf-8");// 设置字符集，必须在connect之前设置
        try {
            ftpClient.connect(ftphost, ftpport);// 地址和端口
        } catch (SocketException e) {
            Log.d(TAG, "ftp connect failed " + e);
            return false;
        } catch (IOException e) {
            // TODO: handle exception
            Log.d(TAG, "ftp connect failed " + e);
            return false;
        }

        Log.d(TAG, "ftp host is " + ftphost);

        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(ftpuname, ftppwd)) {
                Log.d(TAG, "connect FTP service success");
                if (FTPReply.isPositiveCompletion(ftpClient.sendCommand(
                        "OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
                    LOCAL_CHARSET = "UTF-8";
                    Log.d(TAG, "Ftp Mode is UTF-8");
                }
                // ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding(LOCAL_CHARSET);
                ftpClient.enterLocalPassiveMode();// 设置被动模式
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);// 设置传输的模式
                return true;
            }
        }
        disconnect();
        return false;
    }


    public UploadStatus upload2() throws IOException {
        UploadStatus result;
        // 对远程目录的处理
        String local = mBxFile.getFilePath();
        String remotePath = null;
        int type = -1;
        if (BXFile.MimeType.IMAGE == mBxFile.getMimeType()) {
            remotePath = "/Images/";
            type = 1;
        } else if (BXFile.MimeType.MUSIC == mBxFile.getMimeType()) {
            remotePath = "/Audios/";
            type = 2;
        } else if (BXFile.MimeType.VIDEO == mBxFile.getMimeType()) {
            remotePath = "/Videos/";
            type = 3;
        } else {
            remotePath = "/Other/";
        }
        String remote = null;
        String remoteFileName = null;
        String oldFileName = new String(mBxFile.getFileName().getBytes(
                LOCAL_CHARSET), SERVER_CHARSET);
        String httpOldFileName = new String(mBxFile.getFileName().getBytes(
                "UTF-8"), "UTF-8");
        remote = remotePath + Utils.getRemoteFileName(remotePath, httpOldFileName, local);

        remoteFileName = remote;
        if (remote.contains("/")) {
            remoteFileName = Utils.getRemoteFileName(remotePath, httpOldFileName, local);

            // 创建服务器远程目录结构，创建失败直接返回
            if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
                // Log.d(TAG, "create direc failed");
                Log.i("qweqweqwe","新建的方法走了吗    asdasd1 ");
                return UploadStatus.Create_Directory_Fail;
            }
        }
        if (remoteFileName == null){
            Log.i("qweqweqwe","新建的方法走了吗    asdasd 3");
            return UploadStatus.Upload_New_File_Failed;}

        Log.i("qweqweqwe","新建的方法走了吗    asdasd 2");


        File f = new File(local);
        long localSize = f.length();
        result = uploadFile(remoteFileName, f, ftpClient, 0);
        if (!ftpClient.deleteFile(remoteFileName)) {
            return UploadStatus.Delete_Remote_Faild;

        }
        Log.i("QWEQWEA", "删除服务器文件"+localSize);
//        result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
        FTPFile[] files = ftpClient.listFiles(remoteFileName);
        Log.i("qweqweqwe","新建的方法走了吗    asdasd4 ");
        return result;
    }

    /**
     * @return
     * @throws IOException
     * @上传文件
     */
    public UploadStatus upload() throws IOException {
        UploadStatus result;
        // 对远程目录的处理
        String local = mBxFile.getFilePath();
        String remotePath = null;
        int type = -1;
        if (BXFile.MimeType.IMAGE == mBxFile.getMimeType()) {
            remotePath = "/Images/";
            type = 1;
        } else if (BXFile.MimeType.MUSIC == mBxFile.getMimeType()) {
            remotePath = "/Audios/";
            type = 2;
        } else if (BXFile.MimeType.VIDEO == mBxFile.getMimeType()) {
            remotePath = "/Videos/";
            type = 3;
        } else {
            remotePath = "/Other/";
        }
        String remote = null;
        String remoteFileName = null;
        String oldFileName = new String(mBxFile.getFileName().getBytes(
                LOCAL_CHARSET), SERVER_CHARSET);
        String httpOldFileName = new String(mBxFile.getFileName().getBytes(
                "UTF-8"), "UTF-8");
        remote = remotePath + Utils.getRemoteFileName(remotePath, httpOldFileName, local);
        /*
		 * + new String(mBxFile.getFileName().getBytes(LOCAL_CHARSET),
		 * SERVER_CHARSET);
		 */
        remoteFileName = remote;

        if (remote.contains("/")) {
            remoteFileName = Utils.getRemoteFileName(remotePath, httpOldFileName, local);
			/*
			 * new String(mBxFile.getFileName().getBytes( LOCAL_CHARSET),
			 * SERVER_CHARSET);
			 */
            // remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);
            // 创建服务器远程目录结构，创建失败直接返回
            if (CreateDirecroty(remote, ftpClient) == UploadStatus.Create_Directory_Fail) {
                // Log.d(TAG, "create direc failed");
                return UploadStatus.Create_Directory_Fail;
            }
        }

        if (remoteFileName == null)
            return UploadStatus.Upload_New_File_Failed;

        FTPFile[] files = ftpClient.listFiles(remoteFileName);

        if (files.length == 1) {
            long remoteSize = files[0].getSize();
            File f = new File(local);
            long localSize = f.length();
            if (remoteSize == localSize) {
                Log.d(TAG, "file name is " + mBxFile.getFileName()
                        + " progress is " + mBxFile.getFileProgress());
                Log.d(TAG, "服务器中文件等于要上传文件，所以不上传");
                return UploadStatus.File_Exits;
            } else if (remoteSize > localSize) {
                Log.d(TAG, "服务器中文件大于要上传文件，所以不上传");
                return UploadStatus.Remote_Bigger_Local;
            }

            // 尝试移动文件内读取指针,实现断点续传
            result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

            // 如果断点续传没有成功，则删除服务器上文件，重新上传
            if (result == UploadStatus.Upload_From_Break_Failed) {
                if (!ftpClient.deleteFile(remoteFileName)) {
                    return UploadStatus.Delete_Remote_Faild;

                }
                result = uploadFile(remoteFileName, f, ftpClient, 0);
                Log.i("QWEQWEA", "删除服务器文件");

            }
            Log.i("QWEQWEA", "断电续传等于1");
        } else {



            result = uploadFile(remoteFileName, new File(local), ftpClient, 0);
            Log.i("QWEQWEA", "断电续传不等于1");
        }

        if ((UploadStatus.Upload_New_File_Success == result) // 上传成功
                || (UploadStatus.Upload_From_Break_Success == result)) {
            //
            int httpNums = 1;
            httpUtils mHttpUtils = new httpUtils(context, type,
                    httpOldFileName, remoteFileName);
            while (httpNums <= 3) {
                Log.i("qweqweqwe", "1111111111111111111111" + httpNums);
                mHttpUtils.getHttpRequest();

                try {
                    Thread.sleep(3000);
                    if (mHttpUtils.getHttpResult() == 1) {
                        Utils.removeFileName(httpOldFileName);
                        break;
                    } else if (mHttpUtils.getHttpResult() == 0) {
                        httpNums++;
                        Log.i("qweqweqwe", "2222222222222222" + httpNums);

                    } else {
                        Thread.sleep(3000);
                        if (mHttpUtils.getHttpResult() == 1) {
                            Utils.removeFileName(httpOldFileName);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        return result;
    }

    /**
     * 断开与远程服务器的连接
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
            System.out.println("if connected try dis connect");
        }
    }

    /**
     * 递归创建远程服务器目录
     *
     * @param remote    远程服务器文件绝对路径
     * @param ftpClient FTPClient 对象
     * @return 目录创建是否成功
     * @throws IOException
     */
    public UploadStatus CreateDirecroty(String remote, FTPClient ftpClient)
            throws IOException {
        UploadStatus status = UploadStatus.Create_Directory_Success;
        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
        Log.d(TAG, "目录名：" + directory);
        if (!directory.equalsIgnoreCase("/")
                && !ftpClient.changeWorkingDirectory(new String(directory
                .getBytes(LOCAL_CHARSET), SERVER_CHARSET))) {
            // 如果远程目录不存在，则递归创建远程服务器目录
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            while (true) {
                String subDirectory = new String(remote.substring(start, end)
                        .getBytes(LOCAL_CHARSET), SERVER_CHARSET);
                System.out.println("subDirectory = " + subDirectory);
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        Log.e(TAG, "创建目录失败");
                        return UploadStatus.Create_Directory_Fail;
                    }
                }

                start = end + 1;
                end = directory.indexOf("/", start);

                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return status;
    }

    /**
     * @param process 上传过程中文件进程百分比
     */
    public void sendMsg(int process) {

        if (100 == process) {

            mBxFile.setFileState(FileState.UPDATED);
        }
        // 使用下面的语句 好像是可以实时更新 上传状态的 true or false
        if ((process > 0) && (process < 100)) {
            mBxFile.setfUpdatingStatus(true);
        } else {
            mBxFile.setfUpdatingStatus(false);
        }

        synchronized (handler) {
            mBxFile.setFileProgress(process);
            Message message = handler.obtainMessage();
            message.what = PROGRESS;
            message.obj = mBxFile;
            handler.sendMessage(message);
        }

    }

    /**
     * 上传文件到服务器,新上传和断点续传
     *
     * @param remoteFile 远程文件名，在上传之前已经将服务器工作目录做了改变
     * @param localFile  本地文件 File句柄，绝对路径
     *                   需要显示的处理进度步进值
     * @param ftpClient  FTPClient 引用
     * @return
     * @throws IOException
     */
    public UploadStatus uploadFile(String remoteFile, File localFile,
                                   FTPClient ftpClient, long remoteSize) throws IOException {
        UploadStatus status;
        boolean result = false;
        // 显示进度的上传
        long step = localFile.length() / 100;
        long process = 0;
        long localreadbytes = 0L;
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
        OutputStream out = ftpClient.appendFileStream(remoteFile);
        // 断点续传
        if (remoteSize > 0) {
            ftpClient.setRestartOffset(remoteSize);
            process = remoteSize / step;
            raf.seek(remoteSize);
            localreadbytes = remoteSize;
        }
        byte[] bytes = new byte[1024];
        int c;
        while (((c = raf.read(bytes)) != -1) && (mState != FtpState.PAUSE)) {
            out.write(bytes, 0, c);
            localreadbytes += c;

            if (step != 0) {
                if ((localreadbytes / step) != process) {
                    process = localreadbytes / step;
                    // Log.d(TAG, "localFile " + localFile.getName()
                    // + " , update process :" + process);
                    sendMsg((int) process);
                    // 更新数据库信息
                    mDbFile.updataInfos(mBxFile.getFileProgress(),
                            BXFile.FileStateSwitch(mBxFile.getFileState()),
                            mBxFile.getFileName(), mBxFile.getFilePath());
                }
            } else {
                sendMsg(100);
                // 更新数据库信息
                mDbFile.updataInfos(mBxFile.getFileProgress(),
                        BXFile.FileStateSwitch(mBxFile.getFileState()),
                        mBxFile.getFileName(), mBxFile.getFilePath());
            }
        }

        out.flush();
        raf.close();
        out.close();

        if ((ftpClient != null) && (result == false)) {
            result = ftpClient.completePendingCommand();
        }
        if (mState == FtpState.PAUSE) {
            result = false;
        }

        if (remoteSize > 0) {
            status = result ? UploadStatus.Upload_From_Break_Success
                    : UploadStatus.Upload_From_Break_Failed;
        } else {
            status = result ? UploadStatus.Upload_New_File_Success
                    : UploadStatus.Upload_New_File_Failed;
        }

        // Log.d(TAG, "process is " + process + "|exit name is " +
        // mBxFile.getFileName() + "| status is " + status);
        return status;
    }
}
