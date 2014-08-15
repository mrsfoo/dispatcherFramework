package com.zwb.scheduler.dispatcher.junit.util;

import java.util.List;

import com.zwb.scheduler.dispatcher.impl.DispatcherWorkerJobBase;

public class SampleDispatcherWorkerJob02 extends DispatcherWorkerJobBase<String, String>
{
	static long processingDelay = 0;
	static double processingDeviation = 0;

	public static void setProcessionDelay(long delay)
	{
		processingDelay = delay;
	}

	public static void setProcessionDeviation(double deviation)
	{
		processingDeviation = deviation;
	}
	
	@Override
	public void preProcess()
	{
		this.sleep(Util.createDelay(processingDelay, processingDeviation));
	}
	
	@Override
	public void postProcess()
	{
		this.sleep(Util.createDelay(processingDelay, processingDeviation));
	}

	@Override
	public void process()
	{
		this.sleep(Util.createDelay(processingDelay, processingDeviation));
		checkAllInterrupts();
		this.sleep(Util.createDelay(processingDelay, processingDeviation));
		int len = this.getInputData().length();
		String out = new String(this.getInputData());
		out += "[len:" + len + "]";
		this.addOutputData(out);
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
