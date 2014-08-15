package com.zwb.scheduler.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ThreadUtils
{
	
	public static void runInThreadDeprecated(String threadNamePrefix, String name, ExecutorService threadPool, Runnable r)
	{
		Thread t = new Thread(r);
//		t.setDaemon(true);
		t.start();
	}

	public static void runInThread(String threadNamePrefix, String name, ExecutorService threadPool, Runnable r)
	{
		threadPool.execute(r);
	}
	
	public static void setCurrentThreadParams(Runnable r, String prefix, String name)
	{
		nameThread(Thread.currentThread(), r, prefix, name);
	}

	private static void nameThread(Thread t, Runnable r, String prefix, String name)
	{
		String threadName = prefix + "Thr-" + t.getId();
		if ((name != null) && !name.isEmpty())
		{
			threadName += "[" + name + "-" + Integer.toHexString(r.hashCode()) + "]";
		}
		t.setName(threadName);
	}

	public static ExecutorService createThreadPool(final boolean daemon)
	{
		ThreadFactory threadFactory = new ThreadFactory()
		{
			public Thread newThread(Runnable r)
			{
				Thread answer = new Thread(r);
				answer.setDaemon(daemon);
				return answer;
			}
		};
		return Executors.newCachedThreadPool(threadFactory);
	}

}
