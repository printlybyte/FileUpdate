package com.hutu.thread;

import android.util.Log;

import com.hutu.localfile.manager.Updater;


/**
 * 
 * 任务线程
 * 
 * @author: hutuxiansheng
 */
public class TaskThread implements ITaskThread {

	/**
	 * 任务
	 */
	private Updater task;
//	MyThreadPoolExecutor mthreadPool;
	PausableThreadPoolExecutor mthreadPool2;

	/**
	 * 构造函数
	 * 
	 * @param task_
	 */
	public TaskThread(Updater task_, PausableThreadPoolExecutor threadPool) {
		task = task_;
		mthreadPool2 = threadPool;
	}

	/**
	 * 继承与Runnable
	 */
	public void run() {
		// 执行任务
		try {
			task.StartUpdate();
			Log.i("qweqwea","走===========================到着了吗 ");
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("Exception", "Exception : " + e);
		}
		
		//mthreadPool.afterExecute(this, null);
	}
}