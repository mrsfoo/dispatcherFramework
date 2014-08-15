package com.zwb.scheduler.dispatcher.junit.util;

import java.util.ArrayList;
import java.util.List;

import com.zwb.scheduler.dispatcher.api.IDispatcher;
import com.zwb.scheduler.impl.SchedulerJobBase;
import com.zwb.scheduler.util.MyLogger;

public class DispatcherMonitorJob<I, O> extends SchedulerJobBase<List<DispatcherMonitorData>>
{
	private IDispatcher<I, O> dispatcher;
	private long startTimestamp;
	private static MyLogger log = new MyLogger(DispatcherMonitorJob.class);

	public DispatcherMonitorJob(IDispatcher<I, O> dispatcher)
	{
		this.dispatcher = dispatcher;
		this.setResult(new ArrayList<DispatcherMonitorData>());
	}

	@Override
	public void preProcess()
	{
		this.startTimestamp = System.currentTimeMillis();
	}

	@Override
	public void postProcess()
	{
	}

	@Override
	public void process()
	{
		while (true)
		{
			checkAllInterrupts();
			if (this.dispatcher.isRunning())
			{
				long timestamp = System.currentTimeMillis()-startTimestamp;
				List<I> input = this.dispatcher.getInputQueueSnapshot();
				List<O> output = this.dispatcher.getOutputQueueSnapshot();
				List<I> working = this.dispatcher.getWorkingJobQueueSnapshot();
				this.getResult().add(new DispatcherMonitorData(timestamp, input, output, working));
			}
			log.debug("go to sleep");
			this.sleep(this.sleepCycle);
		}
	}

	@Override
	public void onError()
	{
	}

	@Override
	public void onTimeout()
	{
	}

	@Override
	public void onAbort()
	{
	}

}
