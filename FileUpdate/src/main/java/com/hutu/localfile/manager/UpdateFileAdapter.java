package com.hutu.localfile.manager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutu.localfile.manager.BXFile.MimeType;
import com.hutu.localfile.manager.BXFile.UpdatingState;
import com.hutu.localfile.util.FileUtils;
import com.hutu.localfile.util.HProgressBar;
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
public class UpdateFileAdapter extends BaseAdapter {

	private BXFileManager bfm;
	private List<BXFile> data;
	private Context cxt;
	private OnClickListener myListener;
	private String TAG = "UpdateFileAdapter";
	private SyncImageLoader syncImageLoader;
	private List<BXFile> choosedFiles;
	private SyncImageLoader.OnImageLoadListener imageLoadListener;
	
	public Map<Integer, Boolean> checkedALLMap;
	
	public UpdateFileAdapter(List<BXFile> data, Context cxt,
			OnClickListener myListener,
			SyncImageLoader syncImageLoader,
			SyncImageLoader.OnImageLoadListener imageLoadListener) {
		super();
		this.data = data;
		this.cxt = cxt;
		bfm = BXFileManager.getInstance();
		this.syncImageLoader = syncImageLoader;
		this.imageLoadListener = imageLoadListener;
		this.myListener = myListener;
		choosedFiles = bfm.getChoosedFiles();
		
		
		checkedALLMap = new HashMap<Integer, Boolean>();
		for (int i = 0; i < data.size(); i++) {
			
			checkedALLMap.put(i, false);
			
		}
		
	}

	public void refresh(List<BXFile> data) {
		this.data = data;
		
//		this.notifyDataSetChanged();  //7.13更改上传中listview闪屏 图片闪动
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

	// 显示精度条与文件信息
	@Override
	public View getView(int pos, View view, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (null == view) {
			view = LayoutInflater.from(cxt).inflate(R.layout.update_file_item,
					null);
		}

		if (pos >= data.size())
			return null;
		BXFile bxFile = data.get(pos);

		ImageView fileType = (ImageView) view.findViewById(R.id.upfileType);
		
		if ((bxFile.getMimeType().equals(MimeType.IMAGE)) && (FileUtils.isFileExist(bxFile.getFilePath()))) {
			fileType.setImageResource(R.drawable.bxfile_file_default_pic);
			if (null != syncImageLoader && null != imageLoadListener) {
				Log.d(TAG, "updating syncImageLoader is start");
				syncImageLoader.loadDiskImage(pos,
						bxFile.getFilePath(), imageLoadListener);
			}
		} else {
			Log.d(TAG, "updating syncImageLoader not start");
			fileType.setImageResource(bfm.getMimeDrawable(bxFile
					.getMimeType()));
		}

		
		CheckBox fileCheckBox = (CheckBox) view
				.findViewById(R.id.fileCheckBox);
		fileCheckBox.setChecked(choosedFiles.contains(bxFile));
		
		HProgressBar mBar = (HProgressBar) view
				.findViewById(R.id.id_progressbar01);
		mBar.setProgress(bxFile.getFileProgress());

		Button pause_button = (Button) view.findViewById(R.id.pause_button);
		pause_button.setOnClickListener(myListener);
		Button start_button = (Button) view.findViewById(R.id.start_button);
		start_button.setOnClickListener(myListener);
		Log.d(TAG, "updating bxFile is " + bxFile.getFileName() + " and state is "
				+ bxFile.getUpdatingState());
		if (bxFile.getUpdatingState() == UpdatingState.pause) {
			start_button.setVisibility(View.VISIBLE);
			pause_button.setVisibility(View.GONE);
		} else {
			start_button.setVisibility(View.GONE);
			pause_button.setVisibility(View.VISIBLE);
		}
		
		TextView upfileName = (TextView) view.findViewById(R.id.upfileName);
		upfileName.setText(bxFile.getFileName());
		TextView upfilePath = (TextView) view.findViewById(R.id.upfilePath);
		upfilePath.setText(bxFile.getFilePath());

		//
		view.setTag(bxFile.getFilePath());
		// 要是点了全选的图标 设置每个开始按钮不可以点
		if (TbViewManager.pauseAllIsClicked) {
			start_button.setClickable(false);
			//start_button.setVisibility(View.GONE);
			//pause_button.setVisibility(View.GONE);
		}
		//要是文件不在上传状态 就让按钮不可点
//		if (!bxFile.isfUpdatingStatus()) {
//			start_button.setClickable(false);
//			pause_button.setClickable(false);
//		}

		return view;
	}

}
