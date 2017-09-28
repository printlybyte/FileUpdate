package com.hutu.thread;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;

/**
 * 
 * 扩展线程池,在每个任务结束的时候
 * 判断线程池中是否执行完所有的线程任务。
 */
public class MyThreadPoolExecutor extends ThreadPoolExecutor {
	/**
	 * 线程池中任务是否全部结束.
	 */
	private boolean hasFinish = false;

	/**
	 * 构造函数
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param handler
	 */
	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				handler);
	}

    /**
     * 构造函数
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     * @param threadFactory
     * @param handler
     */
	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
	}

	/**
	 * 构造函数
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param threadFactory
	 */
	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory);
	}

	/**
	 * 构造函数
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 */
	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}
	
	private Runnable lastRunable = null;

	public void setAfterExecute(Runnable r) {
		this.lastRunable = r;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.util.concurrent.ThrsseadPoolExecutor#afterExecute(java.lang.Runnable,
	 * java.lang.Throwable)
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		synchronized (this) {
			this.getActiveCount();
			Log.d("afterTask", "count is " + this.getActiveCount());
			if (this.getActiveCount() <= 1)// 已执行完任务之后的最后一个线程
			{
				this.hasFinish = true;
				this.notify();
				if (this.lastRunable != null) {
					lastRunable.run();
				}
			}// if
		}// synchronized
	}

	/**
	 * 功能： 判断是否执行完所有的线程任务，没有执行完的话，主线程挂起
	 *       线程池大小必须>1
	 */
	public boolean isEndTask() {
		synchronized (this) {
			while (!this.hasFinish) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return this.hasFinish;
		}
	}

}