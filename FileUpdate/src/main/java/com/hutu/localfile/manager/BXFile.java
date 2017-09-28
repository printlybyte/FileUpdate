package com.hutu.localfile.manager;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import android.R.bool;
import android.R.integer;
import android.text.TextUtils;
import android.util.Log;

import com.hutu.localfile.util.FileUtils;
import com.hutu.localfile.util.TimeUtils;

/***
 * 
 * @author zhanglei
 *
 */
public class BXFile implements Comparable<BXFile>, Serializable {
	
	public enum MimeType{
		APK,//apk
		TXT,
		IMAGE, //3
		RAR,
		DOC,
		PPT,
		XLS,
		HTML,
		MUSIC,//mp3   1
		VIDEO,//video 2
		PDF,//pdf
		SUPPORT, //video mp3 image 三种类型
		UNKNOWN
	}
	
	//文件发送状态
	public enum FileState{	
		UNUPDATE,//未上传 0
		UPDATING,//上传中 1
		UPDATED,//已上传 2
	}
	
	public enum UpdatingState{
		start,	//开始
		pause, //暂停
		end,   //结束
	}
	
	private BXFile(){}
	
	private String fileName; //文件名
	private String fileUrl; //文件url下载路径
	private String filePath; //文件本地路径
	private boolean isDir;//是否是文件夹
	private long lastModifyTime;//最后修改时间
	private long fileSize;//大小
	private String fileSizeStr;//大小字符串
	private String lastModifyTimeStr;//最后修改的字符串
	private MimeType mimeType;//mimeType
	private FileState fileState;//文件状态
	private UpdatingState mUpdatingState;
	private int fProgress;//文件上传的进度
	private boolean fUpdatingStatus = false;
	
	public boolean isfUpdatingStatus() {
		return fUpdatingStatus;
	}
	
	
	
	public void setfUpdatingStatus(boolean fUpdatingStatus) {
		this.fUpdatingStatus = fUpdatingStatus;
	}

	
	
	
	
	//fixme
	public static int VIDEO_MODE = 1;
	public static int MUSIC_MODE = 2;
	public static int PICTURE_MODE = 3;
	
	public static int LOCAL_MODE = 5;
	
	public boolean isUsefulType() {
		if ((mimeType == MimeType.IMAGE) || (mimeType == MimeType.VIDEO) || (mimeType == MimeType.MUSIC)) {
			return true;
		}
		return false;
	}
	
	public UpdatingState getUpdatingState() {
		return mUpdatingState;
	}
	
	public void  setUpdatingState(UpdatingState mState) {
		this.mUpdatingState = mState;
	}
	
	public FileState getFileState() {
		return fileState;
	}
	public void setFileState(FileState fileState) {
		this.fileState = fileState;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public String getFileName() {
		return fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public boolean isDir() {
		return isDir;
	}
	public long getLastModifyTime() {
		return lastModifyTime;
	}
	public long getFileSize() {
		return fileSize;
	}
	public String getFileSizeStr() {
		return fileSizeStr;
	}
	public MimeType getMimeType() {
		return mimeType;
	}
	public int getFileType() {
		return FileTypeSwitch(mimeType);
	}
	
	public void setFileProgress(int mprogress) {
		this.fProgress = mprogress;
	}
	
	public int getFileProgress() {
		return this.fProgress;
	}
	
	public static int FileTypeSwitch(MimeType mimeType) {
		int type = MUSIC_MODE;
		
		if(mimeType == MimeType.MUSIC) {
			type = MUSIC_MODE;
		}  else if (mimeType == MimeType.VIDEO) {
			type = VIDEO_MODE;
		} else if (mimeType == MimeType.IMAGE) {
			type = PICTURE_MODE;
		} else if (mimeType == MimeType.SUPPORT) {
			type = LOCAL_MODE;
		}
		
		return type;
	}
	
	public static FileState IntSwitchFileState(int type) {
		FileState state = FileState.UNUPDATE;	
		switch (type) {
		case 1:
			state = FileState.UPDATING;
			break;
		case 2:
			state = FileState.UPDATED;
			break;
		default:
			break;
		}
		
		return state;	
	}
	
	public static int FileStateSwitch(FileState state) {
		int type = 0; //unupdate
		
		if(state == FileState.UNUPDATE) {
			type = 0;
		}  else if (state == FileState.UPDATING) {
			type = 1;
		} else if (state == FileState.UPDATED) {
			type = 2;
		} 
		return type;
		
		
	}
	
	public String getLastModifyTimeStr() {
		return lastModifyTimeStr;
	}
	
	/*
	 *通过文件名判断该文件是否是支持的文件 
	 */
	public static boolean getFileExspansion(String filepath) {
		//File mFile = new File(filepath);	
		String exspansion = FileUtils.getExspansion(filepath);
		
		MimeType mimeType = MimeType.UNKNOWN;	
		if(TextUtils.isEmpty(exspansion))
			mimeType = MimeType.UNKNOWN;
		else{
			mimeType = BXFileManager.getInstance().getMimeType(exspansion);
			mimeType = (null==mimeType)?MimeType.UNKNOWN:mimeType;
			if ((mimeType == MimeType.IMAGE) || (mimeType == MimeType.VIDEO) || (mimeType == MimeType.MUSIC)) {
				return true;
			}			
		}
		
		
		return false;
	}


	//本地文件build模式
	public static class Builder{
		BXFile bxFile;
		
		public Builder(String path){
			if(FileUtils.isFileExist(path)){
				File file = new File(path); 
				bxFile = new BXFile();
				bxFile.fileName  = file.getName();
				bxFile.filePath = file.getAbsolutePath();

				boolean isDir = file.isDirectory();
				bxFile.isDir = isDir;
				if(!isDir){
					bxFile.fProgress = 0;
					bxFile.fileSize = file.length();
					bxFile.fileState = FileState.UNUPDATE;
					bxFile.fileSizeStr = FileUtils.getFileSizeStr(bxFile.fileSize);
					bxFile.lastModifyTime = file.lastModified();
					bxFile.lastModifyTimeStr = TimeUtils.getDateTime(bxFile.lastModifyTime);
					String exspansion = FileUtils.getExspansion(bxFile.fileName);
					if(TextUtils.isEmpty(exspansion))
						bxFile.mimeType = MimeType.UNKNOWN;
					else{
						MimeType mimeType = BXFileManager.getInstance().getMimeType(exspansion);
						bxFile.mimeType = (null==mimeType)?MimeType.UNKNOWN:mimeType;
						if (bxFile.mimeType == MimeType.UNKNOWN) {
							Log.d("BxFile", "unkonwn type is " + exspansion);
						}
					}
				}
			}
			
		}
		
		
		
		public BXFile build(){
			return bxFile;
		}
		
		public BXFile build(int progress) {
			bxFile.fProgress = progress;
			return bxFile;
		}
	}
	
	//url文件builder模式(用于短消息附带的文件信息初始化)
	public static class UrlBuilder{
		BXFile bxFile;
		
		public UrlBuilder(String fileUrl , String fileName , long fileSize , String savedPath , FileState fileState){
			bxFile = new BXFile();
			bxFile.fileUrl = fileUrl;
			bxFile.fileName = fileName;
			bxFile.fileSize = fileSize;
			bxFile.fileState = fileState;
			bxFile.fileSizeStr = FileUtils.getFileSizeStr(fileSize);
			bxFile.filePath = savedPath;
			String exspansion = FileUtils.getExspansion(fileName);
			if(TextUtils.isEmpty(exspansion))
				bxFile.mimeType = MimeType.UNKNOWN;
			else{ 
				MimeType mimeType = BXFileManager.getInstance().getMimeType(exspansion);
				bxFile.mimeType = null==mimeType?MimeType.UNKNOWN:mimeType;
			}
		}
		
		public BXFile build(){
			return bxFile;
		}
	}

	@Override
	public int compareTo(BXFile another) {
		if(isDir()){
			if(another.isDir())
				return fileName.compareToIgnoreCase(another.getFileName());
			else
				return -1;
		}else{
			if(another.isDir())
				return 1;
			else
				return fileName.compareToIgnoreCase(another.getFileName());
		}
	}
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(null == o)
			return false;
		if(o instanceof BXFile){
			BXFile other = (BXFile)o;
			return other.filePath.equals(filePath);
		}else{
			return false;
		}
	}
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return filePath.hashCode();
	}
}
