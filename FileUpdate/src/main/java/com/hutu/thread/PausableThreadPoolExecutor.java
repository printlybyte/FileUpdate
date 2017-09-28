package com.hutu.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

public class PausableThreadPoolExecutor extends ThreadPoolExecutor {
	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	private Condition unpaused = pauseLock.newCondition();

	private boolean hasFinish = false;

	/**
	 * 构造函数 可以暂停的线程池
	 */
	public PausableThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		pauseLock.lock();
		try {
			while (isPaused)
				unpaused.await();
		} catch (InterruptedException ie) {
			t.interrupt();
		} finally {
			pauseLock.unlock();
		}

	}

	private Runnable lastRunable = null;

	public void setAfterExecute(Runnable r) {
		this.lastRunable = r;
	}

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
	 * 功能： 判断是否执行完所有的线程任务，没有执行完的话，主线程挂起 线程池大小必须>1
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

	public void pause() {
		System.out.println("p11111111");
		pauseLock.lock();
		try {
			System.out.println("p2222222");
			isPaused = true;

		} finally {
			pauseLock.unlock();
			System.out.println("p333333333333");
		}

	}

	public void resume() {
		pauseLock.lock();
		try {
			isPaused = false;
			unpaused.signalAll();
		} finally {
			pauseLock.unlock();
		}
	}
}