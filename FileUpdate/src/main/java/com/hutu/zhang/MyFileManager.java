package com.hutu.zhang;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hutu.localfile.util.FileUtils;
import com.hutu.localfile.util.Utils;
import com.hutu.localfileupdate.MainActivity;
import com.hutu.localfileupdate.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyFileManager extends ListActivity implements OnClickListener{
	/**文件（夹）名字*/
	private List<String> items = null;
	/**文件（夹）路径*/
	private List<String> paths = null;
	/**根目录**/
	private String rootPath = "/";
	/**当前目录**/
	private String curPath = "/mnt/";
	/**显示当前目录**/
	private TextView mPath;


	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);  //FEATURE_CUSTOM_TITLE,FEATURE_NO_TITLE

		setContentView(R.layout.fileselect);
		mPath = (TextView) findViewById(R.id.mPath); 
		findViewById(R.id.buttonConfirm).setOnClickListener(this);
		findViewById(R.id.buttonCancle).setOnClickListener(this);
		 
		getFileDir(curPath);
	}

	/**
	 * 获取指定目录下的所有文件(夹)
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath); 
		File[] files = f.listFiles();

		//用来显示 “返回根目录”+"上级目录" 
		if (!filePath.equals(rootPath)) {
			items.add("rootPath");
			paths.add(rootPath);
			
			items.add("parentPath");
			paths.add(f.getParent());
		}
		
		
		//先排序
		List<File> resultList =null ;
		if(files!=null){
			Log.i("hnyer", files.length+ " "+filePath) ;
			resultList = new ArrayList<File>();
			int DirectoryCount=0;
			for (int i = 0;   i < files.length; i++) {
				File file = files[i];  
				//测试
				if (file.isDirectory()) {
					DirectoryCount++;
				}
				if(!file.getName().startsWith(".")){
					resultList.add(file) ;
				}
			}
			
			if (DirectoryCount==0) {
				Log.d("输出", "最后一层");
				Toast.makeText(this, "已经是最后一个文件夹", Toast.LENGTH_SHORT).show();
			}
			
			//
			Collections.sort(resultList, new Comparator<File>() {
                @Override
                public int compare(File bean1, File bean2) {
                    return bean1 .getName().toLowerCase().compareTo(bean2.getName().toLowerCase() )  ;
                     
                }
            });
			
			for (int i = 0;   i < resultList.size(); i++) {
				File file = resultList.get(i) ;   
				items.add(file.getName());
				paths.add(file.getPath());
			}
		}else{
			Log.i("hnyer", filePath+"无子文件") ;
			Toast.makeText(this, "已经是最后一个文件夹", Toast.LENGTH_SHORT).show();
		}

		setListAdapter(new MyAdapter(this, items, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(paths.get(position));
		if (file.isDirectory()) {
			curPath = paths.get(position);
			getFileDir(paths.get(position));
		} else {
			openFile(file);
		}
	}

	private void openFile(File f) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(f);
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.buttonConfirm:

			//在这里的代码里，弹出一个dialog然后把目录传到bundle里面，在其他的activity里可以取
			Intent data = new Intent(MyFileManager.this, MainActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("file", curPath);
			data.putExtras(bundle);
			setResult(2, data); 
			System.out.println("点完弹出设置默认目录确定键之后curpath的数据是"+bundle.get("file")); //这边是有数据的，good news

		//点击确定的时候删除一下改文件下的隐藏文件夹
			FileUtils.delHindenFile(curPath);

			String mDirString = Utils.getDefautDirOp();
			int index = Integer.parseInt(mDirString); 
			
			
			//FUCK YEAH,FINALLY FIX THAT BUG!! THAT WAS NOT UPDATE FILE NAME AND FILE NUMBERS  IMMEDIATELY
			//修复不能 弹出 窗口 之后不能即使更新页面的BUG
			
			
			Utils.setPreferences(Utils.DefaultPath[index-1], (String) bundle.get("file"));
			
			Log.d("new--MyFileManager", "key=="+Utils.DefaultPath[index-1]+",value=="+(String) bundle.get("file"));

		
			
			
			finish();

		
			break;
		case R.id.buttonCancle:
			finish();
			break;

		}
		
	}
}