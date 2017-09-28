/**   
 * @Title: DbFile.java 
 * @Package com.android.updatadb 
 * @Description: TODO
 * @author Long Li  
 * @date 2015-4-30 下午5:20:56 
 * @version V1.0   
 */
package com.hutu.databases;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutu.localfile.DefaultPage;
import com.hutu.localfile.manager.BXFile;
import com.hutu.localfile.manager.BXFile.FileState;
import com.hutu.localfile.manager.BXFile.MimeType;
import com.hutu.localfile.manager.ListenerManager;
import com.hutu.localfile.manager.Updater;

/**
 * @author hutuxiansheng
 * @version: V1.0
 */
public class DbFile {

	private Context mConext;
	private String TAG = "longli";
	private static DbFile mDb = null;

	public DbFile(Context context) {
		mConext = context;
	}

	public static DbFile getDbInstance(Context context) {
		if (null == mDb) {
			mDb = new DbFile(context);
		}
		return mDb;
	}

	public SQLiteDatabase getConnection() {
		DBHelper mDbHelper = new DBHelper(mConext);
		SQLiteDatabase msql = mDbHelper.getReadableDatabase();
		return msql;
	}

	// 检查文件是否存在
	public synchronized boolean isHasFileInfos(String fileName, String filepath) {
		SQLiteDatabase database = getConnection();
		int count = -1;

		Cursor cursor = null;

		try {
			String sql = "select count(*)  from localfile_info where fName=? and fPath=?";
			cursor = database
					.rawQuery(sql, new String[] { fileName, filepath });
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
				// Log.d("long", "count is " + count);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (null != database) {
				database.close();
			}
			if (null != cursor) {
				cursor.close();
			}
		}

		if (count > 0)
			return true;

		return false;
	}

	// 创建文件信息
	public synchronized void saveInfos(List<BXFile> infos) {
		SQLiteDatabase database = getConnection();
		try {
			for (BXFile info : infos) {
				String sql = "insert into localfile_info(fName, fPath, fSize, fType, fState, fProgress, fTime) values (?,?,?,?,?,?,?)";
				Object[] bindArgs = { info.getFileName(), info.getFilePath(),
						info.getFileSize(), info.getFileType(), 0, 0,
						info.getLastModifyTime() };
				database.execSQL(sql, bindArgs);
			}
		} catch (Exception e) {
			Log.e(TAG, "save info failed " + e);
		} finally {
			if (null != database) {
				database.close();
			}
		}
	}

	/*
	 * 得到上传文件的具体信息
	 */
	public synchronized BXFile getFileInfos(String filename, String filepath) {
		//List<BXFile> list = new ArrayList<BXFile>();
		BXFile bxfile = null;
		SQLiteDatabase database = getConnection();
		Cursor cursor = null;
		try {
			String sql = "select fName, fPath, fSize, fState, fProgress, fTime from localfile_info where fName=? and fPath=?";
			cursor = database
					.rawQuery(sql, new String[] { filename, filepath });
		
			while (cursor.moveToNext()) {
				BXFile.Builder builder = new BXFile.Builder(cursor.getString(1)); //通过绝对路径来获取文件信息
				bxfile = builder.build(cursor.getInt(4)); // 将上传的文件进度复制进去
				bxfile.setFileState(BXFile.IntSwitchFileState(Integer.valueOf(cursor.getString(3))));
				if (null != bxfile) {
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (null != database) {
				database.close();
			}
			if (null != cursor) {
				cursor.close();
			}
		}


		return bxfile;
	}
	
	
	
	
	/*
	 * 更新本地数据库，发现新的文件
	 */
	public synchronized void updateDbFile(List<BXFile> mdata) {
		List<BXFile> mBxFiles = new ArrayList<BXFile>();
		for (BXFile mfile : mdata) {
			if (getFileInfos(mfile.getFileName(), mfile.getFilePath()) == null) {
				Log.d(TAG, "find new file " + mfile.getFilePath());
				mBxFiles.add(mfile);
				
				
			}
		
		}
		
		saveInfos(mBxFiles);
	}

	/*
	 * 根据文件上传状态和文件类型来获取文件
	 */
	public synchronized List<BXFile> getDifTypeInfos(MimeType type,
			FileState state) {
		int mtype = BXFile.FileTypeSwitch(type);
		int mstate = BXFile.FileStateSwitch(state);
		List<BXFile> list = new ArrayList<BXFile>();
		SQLiteDatabase database = getConnection();
		Cursor cursor = null;
		String sql = null;
		try {

			if (MimeType.SUPPORT != type) {
				sql = "select fName, fPath, fSize, fState, fProgress, fTime from localfile_info where fType=? and fState=?";
				cursor = database.rawQuery(sql, new String[] { mtype + "",
						mstate + "" });
			} else {
				sql = "select fName, fPath, fSize, fState, fProgress, fTime from localfile_info where (fType=? or fType=? or fType=?) and fState=?";
				cursor = database.rawQuery(sql, new String[] {
						BXFile.MUSIC_MODE + "", BXFile.PICTURE_MODE + "",
						BXFile.VIDEO_MODE + "", mstate + "" });
			}

			while (cursor.moveToNext()) {
				BXFile.Builder builder = new BXFile.Builder(cursor.getString(1)); // 通过绝对路径来获取文件信息
				BXFile bxfile = builder.build(cursor.getInt(4)); // 将上传的文件进度复制进去
				bxfile.setFileProgress(cursor.getInt(4));
				bxfile.setFileState(state);
				if (null != bxfile)
					list.add(bxfile);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (null != database) {
				database.close();
			}
			if (null != cursor) {
				cursor.close();
			}
		}
		// if (list.size() == 0)
		// return null;

		return list;
	}

	/*
	 * 批量更新文件状态
	 */
	public synchronized void updateDbInfos(int progress, int fstate,
			List<BXFile> mdata) {
		for (BXFile mFile : mdata) {
			Log.d(TAG, "start update infos.." + mFile.getFilePath()
					+ " state is " + fstate);
			updataInfos(progress, fstate, mFile.getFileName(),
					mFile.getFilePath());
		}
	}
	
	//删除数据库中记录
	public synchronized void deletInfo(String filename, String filepath) {
		SQLiteDatabase database = getConnection();
		try {
			String sql = "delete from localfile_info where fName=? and fPath=?";
			Object[] bindArgs = {filename, filepath };
			database.execSQL(sql, bindArgs);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "delet info failed " + e);
		} finally {
			if (null != database) {
				database.close();
			}
		}
	}

	/*
	 * 上传过程中更新进度与状态
	 */
	public synchronized void updataInfos(int progress, int fstate,
			String filename, String filepath) {
		SQLiteDatabase database = getConnection();
		try {
			String sql = "update localfile_info set fProgress=?, fState=? where fName=? and fPath=?";
			Object[] bindArgs = { progress, fstate, filename, filepath };
			database.execSQL(sql, bindArgs);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "update infos failed " + e);
		} finally {
			if (null != database) {
				database.close();
				
			}
		}
	}
}
