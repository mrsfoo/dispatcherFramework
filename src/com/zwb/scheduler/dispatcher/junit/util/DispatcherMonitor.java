package com.zwb.scheduler.dispatcher.junit.util;

import java.util.List;

import com.zwb.scheduler.api.IScheduler;
import com.zwb.scheduler.api.SchedulerFactory;
import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.dispatcher.api.IDispatcher;
import com.zwb.scheduler.util.MyLogger;

public class DispatcherMonitor<I,O>
{
	private IScheduler scheduler;
	private DispatcherMonitorJob<I,O> dispatcherMonitorJob;
	IDispatcher<I,O> dispatcher;
	private static MyLogger log = new MyLogger(DispatcherMonitor.class);

	public DispatcherMonitor(IDispatcher<I, O> dispatcher, long timeGran)
	{
		this.dispatcher = dispatcher;
		scheduler = SchedulerFactory.createScheduler();
		scheduler.setMaxThread(1);
		scheduler.setName("DispatcherMonitorSched");
		scheduler.setSleepCycle(timeGran);
		dispatcherMonitorJob = new DispatcherMonitorJob<I,O>(dispatcher);
		dispatcherMonitorJob.setName("DispatcherMonitorJob");
		dispatcherMonitorJob.setSleepCycle(timeGran);
	}
	
	public void startAsync()
	{
		scheduler.start();
		scheduler.executeAsync(dispatcherMonitorJob);
	}

	public void startSync()
	{
		this.startAsync();
		this.waitUntilRunning();
	}

	public void stopAsync()
	{
		dispatcherMonitorJob.abortAsync();
		scheduler.stop();
	}
	
	public void stopSync()
	{
		dispatcherMonitorJob.abortAsync();
		this.waitUntilStopped();
		scheduler.stop();
	}
	
	public void waitUntilRunning()
	{
		boolean timeout = this.dispatcherMonitorJob.waitForStates(5000, SchedulerJobState.RUNNING);
		if (timeout)
		{
			throw new RuntimeException("Timeout!");
		}
	}
	
	public void waitUntilStopped()
	{
		boolean timeout = this.dispatcherMonitorJob.waitForStates(5000, SchedulerJobState.ABORTED);
		if (timeout)
		{
			throw new RuntimeException("Timeout!");
		}
	}
	
	public void printFormatted()
	{
		int len = 5;
		List<DispatcherMonitorData> data = this.dispatcherMonitorJob.getResult();
		String s = "\n";
		s += "########################################################"+"\n";
		s += "Dispatcher Monitor Data:"+"\n";
		s += "########################################################"+"\n";
		for(DispatcherMonitorData d: data)
		{
			s += "[" + workTS(d.timestamp,len) + "] InputQueue   : " + d.inputQueue+"\n";
			s += "          RunningQueue : " + d.workingQueue+"\n";
			s += "          OutputQueue  : " + d.outputQueue+"\n";
		}
		s += "########################################################"+"\n";
		log.info(s);
	}

	public String workTS(long ts, int len)
	{
		String s = Long.toString(ts);
		while(s.length()<len)
		{
			s = "0"+s;
		}
		
		String ss = s.substring(0, len-3) + "." + s.substring(len-3);
		ss = ss+"s";
		return ss;
	}

}
