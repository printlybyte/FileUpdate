package com.hutu.localfile.manager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutu.localfile.manager.BXFile.FileState;
import com.hutu.localfile.manager.BXFile.MimeType;
import com.hutu.localfile.util.FileUtils;
import com.hutu.localfileupdate.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地文件adapter
 * 
 * @author hutu
 * 
 */
public class LocalFileAdapter extends BaseAdapter {

	private BXFileManager bfm;
	private List<BXFile> data;
	private Context cxt;
	private List<BXFile> choosedFiles;
	int w;
	private SyncImageLoader syncImageLoader;
	private SyncImageLoader.OnImageLoadListener imageLoadListener;
	private String TAG = "LocalFIleAdapter";

	public Map<Integer, Boolean> checkedALLMap;

	public LocalFileAdapter(List<BXFile> data, Context cxt,
			SyncImageLoader syncImageLoader,
			SyncImageLoader.OnImageLoadListener imageLoadListener) {
		super();
		this.data = data;
		this.cxt = cxt;
		this.syncImageLoader = syncImageLoader;
		this.imageLoadListener = imageLoadListener;
		bfm = BXFileManager.getInstance();
		choosedFiles = bfm.getChoosedFiles();
		w = cxt.getResources().getDimensionPixelSize(R.dimen.view_36dp);

		checkedALLMap =new HashMap<Integer, Boolean>() ;
		for (int i = 0; i < data.size(); i++) {
			
			checkedALLMap.put(i, false);
			
		}

	}

	public void refresh(List<BXFile> data) {
		this.data = data;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (null != data)
			return data.size();
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void initUnupdateView(int pos, View view, BXFile bxFile) {

		View dirView = view.findViewById(R.id.dirRl);
		TextView dirName = (TextView) view.findViewById(R.id.dirName);

		View fileView = view.findViewById(R.id.fileLl);
		dirView.setVisibility(View.GONE);
		fileView.setVisibility(View.VISIBLE);

		CheckBox fileCheckBox = (CheckBox) view.findViewById(R.id.fileCheckBox);

		MimeType mfileType = bxFile.getMimeType();
		if ((mfileType != MimeType.MUSIC) && (mfileType != MimeType.VIDEO)
				&& (mfileType != MimeType.IMAGE)) {
			fileCheckBox.setVisibility(View.GONE);
		} else {
			fileCheckBox.setVisibility(View.VISIBLE);

			fileCheckBox.setChecked(choosedFiles.contains(bxFile));

		}

		ImageView fileType = (ImageView) view.findViewById(R.id.fileType);
		TextView fileName = (TextView) view.findViewById(R.id.fileName);
		TextView fileSize = (TextView) view.findViewById(R.id.fileSize);
		TextView fileModifyDate = (TextView) view
				.findViewById(R.id.fileModifyDate);
		fileName.setText(bxFile.getFileName());
		fileSize.setText(bxFile.getFileSizeStr());
		fileModifyDate.setText(bxFile.getLastModifyTimeStr());
		if ((bxFile.getMimeType().equals(MimeType.IMAGE))
				&& (FileUtils.isFileExist(bxFile.getFilePath()))) {
			fileType.setImageResource(R.drawable.bxfile_file_default_pic);
			if (null != syncImageLoader && null != imageLoadListener) {
				// Log.d(TAG, "syncImageLoader is start");
				syncImageLoader.loadDiskImage(pos, bxFile.getFilePath(),
						imageLoadListener);
			}
		} else {
			fileType.setImageResource(bfm.getMimeDrawable(bxFile.getMimeType()));
		}
	}

	private void initUpdatedView(int pos, View view, BXFile bxFile) {
		View dirView = view.findViewById(R.id.dirRl);
		TextView dirName = (TextView) view.findViewById(R.id.dirName);

		View fileView = view.findViewById(R.id.fileLl);
		dirView.setVisibility(View.GONE);
		fileView.setVisibility(View.VISIBLE);

		CheckBox fileCheckBox = (CheckBox) view.findViewById(R.id.fileCheckBox);
		fileCheckBox.setChecked(choosedFiles.contains(bxFile));
		// fileCheckBox.setVisibility(View.GONE);

		ImageView fileType = (ImageView) view.findViewById(R.id.fileType);
		TextView fileName = (TextView) view.findViewById(R.id.fileName);
		TextView fileSize = (TextView) view.findViewById(R.id.fileSize);
		TextView fileModifyDate = (TextView) view
				.findViewById(R.id.fileModifyDate);
		fileName.setText(bxFile.getFileName());
		fileSize.setText(bxFile.getFileSizeStr());
		fileModifyDate.setText(bxFile.getLastModifyTimeStr());
		if ((bxFile.getMimeType().equals(MimeType.IMAGE))
				&& (FileUtils.isFileExist(bxFile.getFilePath()))) {
			fileType.setImageResource(R.drawable.bxfile_file_default_pic);
			if (null != syncImageLoader && null != imageLoadListener) {
				// Log.d("longd", "updated is load disimg " +
				// bxFile.getFilePath());
				syncImageLoader.loadDiskImage(pos, bxFile.getFilePath(),
						imageLoadListener);
			}
		} else {
			fileType.setImageResource(bfm.getMimeDrawable(bxFile.getMimeType()));
		}
		// fileCheckBox.setChecked(choosedFiles.contains(bxFile));//
		// 是否勾选chechBox
	}

	// 目录：显示目录view;文件：显示文件view及勾选状况
	@Override
	public View getView(int pos, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (null == view) {
			view = LayoutInflater.from(cxt).inflate(R.layout.locale_file_item,
					null);
		}

		view.setTag(pos);
		BXFile bxFile = data.get(pos);
		if (bxFile.isDir()) {
			View dirView = view.findViewById(R.id.dirRl);
			TextView dirName = (TextView) view.findViewById(R.id.dirName);
			View fileView = view.findViewById(R.id.fileLl);
			dirView.setVisibility(View.VISIBLE);
			dirName.setText(bxFile.getFileName());
			fileView.setVisibility(View.GONE);
		} else {
			if (bxFile.getFileState() == FileState.UNUPDATE) {
				initUnupdateView(pos, view, bxFile);
			} else if (bxFile.getFileState() == FileState.UPDATED) {
				initUpdatedView(pos, view, bxFile); // fixme
			}
		}
		return view;
	}

}
