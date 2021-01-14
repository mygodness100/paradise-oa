package com.wy.manager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.wy.util.SpringContextUtils;
import com.wy.util.Threads;

/**
 * 异步任务管理器
 *
 * @author ParadiseWY
 * @date 2020年4月8日 上午12:30:11
 */
public class AsyncManager {

	/**
	 * 操作延迟10毫秒
	 */
	private final int OPERATE_DELAY_TIME = 10;

	/**
	 * 异步操作任务调度线程池
	 */
	private ScheduledExecutorService executor = (ScheduledExecutorService) SpringContextUtils
			.getBean("scheduledExecutorService");

	/**
	 * 单例模式
	 */
	private AsyncManager() {
	}

	private static AsyncManager me = new AsyncManager();

	public static AsyncManager me() {
		return me;
	}

	/**
	 * 执行任务
	 * 
	 * @param task 任务
	 */
	public void execute(TimerTask task) {
		executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
	}

	/**
	 * 停止任务线程池
	 */
	public void shutdown() {
		Threads.shutdownAndAwaitTermination(executor);
	}
}