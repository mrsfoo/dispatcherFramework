package com.zwb.scheduler.api;

import com.zwb.scheduler.impl.Scheduler;

public class SchedulerFactory
{
	public static IScheduler createScheduler()
	{
		Scheduler d = new Scheduler();
		return d;
	}

}
