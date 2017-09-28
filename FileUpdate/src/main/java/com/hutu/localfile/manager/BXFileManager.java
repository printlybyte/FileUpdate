package com.hutu.localfile.manager;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.hutu.localfile.manager.BXFile.MimeType;
import com.hutu.localfile.util.FileUtils;
import com.hutu.localfileupdate.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BXFileManager {

	private static BXFileManager instance;
	private final Map<String, MimeType> map;
	private final Map<MimeType, Integer> resMap;
	private final List<BXFile> choosedFiles;

	public static BXFileManager getInstance() {
		if (null == instance) {
			instance = new BXFileManager();
		}
		return instance;
	}

	private BXFileManager() {
		map = new HashMap<String, MimeType>();
		map.put(".amr", MimeType.MUSIC);
		map.put(".mp3", MimeType.MUSIC);
		map.put(".m4a", MimeType.MUSIC);
		map.put(".aac", MimeType.MUSIC);
		map.put(".ogg", MimeType.MUSIC);
		map.put(".wav", MimeType.MUSIC);
		map.put(".mkv", MimeType.MUSIC);
		map.put(".flac", MimeType.MUSIC);

		map.put(".3gp", MimeType.VIDEO);
		map.put(".mp4", MimeType.VIDEO);
		map.put(".rmvb", MimeType.VIDEO);
		map.put(".mpeg", MimeType.VIDEO);
		map.put(".mpg", MimeType.VIDEO);
		map.put(".asf", MimeType.VIDEO);
		map.put(".avi", MimeType.VIDEO);
		map.put(".wmv", MimeType.VIDEO);

		map.put(".apk", MimeType.APK);

		map.put(".bmp", MimeType.IMAGE);
		map.put(".gif", MimeType.IMAGE);
		map.put(".jpeg", MimeType.IMAGE);
		map.put(".jpg", MimeType.IMAGE);
		map.put(".png", MimeType.IMAGE);

		map.put(".doc", MimeType.DOC);
		map.put(".docx", MimeType.DOC);
		map.put(".rtf", MimeType.DOC);
		map.put(".wps", MimeType.DOC);
		map.put(".xls", MimeType.XLS);
		map.put(".xlsx", MimeType.XLS);
		map.put(".gtar", MimeType.RAR);
		map.put(".gz", MimeType.RAR);
		map.put(".zip", MimeType.RAR);
		map.put(".tar", MimeType.RAR);
		map.put(".rar", MimeType.RAR);
		map.put(".jar", MimeType.RAR);
		map.put(".htm", MimeType.HTML);
		map.put(".html", MimeType.HTML);
		map.put(".xhtml", MimeType.HTML);
		map.put(".java", MimeType.TXT);
		map.put(".txt", MimeType.TXT);
		map.put(".xml", MimeType.TXT);
		map.put(".log", MimeType.TXT);
		map.put(".pdf", MimeType.PDF);
		map.put(".ppt", MimeType.PPT);
		map.put(".pptx", MimeType.PPT);

		resMap = new HashMap<MimeType, Integer>();
		resMap.put(MimeType.APK, R.drawable.bxfile_file_apk);
		resMap.put(MimeType.DOC, R.drawable.bxfile_file_doc);
		resMap.put(MimeType.HTML, R.drawable.bxfile_file_html);
		resMap.put(MimeType.IMAGE, R.drawable.bxfile_file_unknow);
		resMap.put(MimeType.MUSIC, R.drawable.bxfile_file_mp3);
		resMap.put(MimeType.VIDEO, R.drawable.bxfile_file_video);
		resMap.put(MimeType.PDF, R.drawable.bxfile_file_pdf);
		resMap.put(MimeType.PPT, R.drawable.bxfile_file_ppt);
		resMap.put(MimeType.RAR, R.drawable.bxfile_file_zip);
		resMap.put(MimeType.TXT, R.drawable.bxfile_file_txt);
		resMap.put(MimeType.XLS, R.drawable.bxfile_file_xls);
		resMap.put(MimeType.UNKNOWN, R.drawable.bxfile_file_unknow);

		choosedFiles = new ArrayList<BXFile>();
	}

	public MimeType getMimeType(String exspansion) {
		return map.get(exspansion.toLowerCase());
	}

	public Integer getMimeDrawable(MimeType type) {
		return resMap.get(type);
	}

	public List<BXFile> getChoosedFiles() {
		return choosedFiles;
	}

	public String getFilesSizes() {
		long sum = 0;
		for (BXFile f : choosedFiles) {
			sum += f.getFileSize();
		}
		return FileUtils.getFileSizeStr(sum);
	}

	public int getFilesCnt() {
		return choosedFiles.size();
	}

	public void clear() {
		choosedFiles.clear();
	}

	public synchronized List<BXFile> getMediaFiles(Activity cxt, Uri uri) {
		Cursor mCursor = cxt.managedQuery(uri,
				new String[] { MediaStore.Audio.Media.DATA }, null, null,
				" date_modified desc");
		cxt.startManagingCursor(mCursor);
		int count = mCursor.getCount();
		if (count > 0) {
			List<BXFile> data = new ArrayList<BXFile>();
			if (mCursor.moveToFirst()) {
				do {
					BXFile.Builder builder = new BXFile.Builder(
							mCursor.getString(0));
					BXFile bxfile = builder.build();
					if (null != bxfile)
						data.add(bxfile);
				} while (mCursor.moveToNext());
			}
			return data;
		} else {
			return null;
		}
	}

	public synchronized int getMediaFilesCnt(Activity cxt, Uri uri) {
		Cursor mCursor = cxt.managedQuery(uri,
				new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
		cxt.startManagingCursor(mCursor);
		int cnt = mCursor.getCount();
		return cnt;
	}
	/**
	 * @param root 设置的目录的地址
	 * @return 得到默认目录下的文件
	 */
	public synchronized List<BXFile> getAutoLocalFiles(String root) {
		List<String> files = new ArrayList<String>();
		List<BXFile> bxFiles = new ArrayList<BXFile>();
		FileUtils.getAllFiles(new File(root), files);
		Log.d("hutuxiansheng", "all file size is " + files.size());
		for (String mFile : files) {
			if (BXFile.getFileExspansion(mFile)) {
				BXFile.Builder builder = new BXFile.Builder(mFile);
				BXFile bxfile = builder.build();
				bxFiles.add(bxfile); // 支持的类型文件
				Log.i("QWEQWEA",""+bxfile.isDir());
			}else {
//                Log.i("QWEQWEA",""+bxfile.isDir());
            }
		}
		Log.i("hutuxiansheng", "get over  " + files.size());  //得到文件个数
		return bxFiles;
	}

	public synchronized int getAutoLocalFilesCnt(String root) {
		List<String> files = new ArrayList<String>();
		FileUtils.getAllFiles(new File(root), files);
		return files.size();
	}

}
