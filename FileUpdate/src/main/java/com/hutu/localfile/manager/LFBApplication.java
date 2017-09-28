package com.hutu.localfile.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;

public class LFBApplication extends Application {

	private ExecutorService es = Executors.newFixedThreadPool(3);
	private static LFBApplication myApplication;

	// app对外 执行任务入口
	public void execRunnable(Runnable r) {
		if (!es.isShutdown()) {
			es.execute(r);
		}
	}

	public void onCreate() {

		super.onCreate();
		myApplication = this;
	}
	
	public static LFBApplication getApplication(){
		return myApplication;
	}
	
}
