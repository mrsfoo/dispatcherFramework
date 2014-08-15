package com.zwb.scheduler.junit.util;

import com.zwb.scheduler.impl.SchedulerJobBase;
import com.zwb.scheduler.util.MyLogger;

public class SampleJob extends SchedulerJobBase<Long>
{
	private static MyLogger log = new MyLogger(SampleJob.class);

	private long counter = 0;
	private boolean preProcessed = false;
	private boolean postProcessed = false;
	private boolean processed = false;
	private boolean aborted = false;
	private boolean timedOut = false;
	private boolean error = false;
	private long max;
	private long preProcessingDelay = 420;
	private long postProcessingDelay = 240;
	
	public SampleJob(long max)
	{
		this.max = max;
	}
	
	@Override
	public void preProcess()
	{
		this.preProcessed = true;
		log.info("...pre-processing "+this.getName());
		this.sleep(this.preProcessingDelay);
	}
	
	@Override
	public void postProcess()
	{
		this.postProcessed = true;
		log.info("...post-processing "+this.getName());
		this.sleep(this.postProcessingDelay);
	}

	@Override
	public void process()
	{
		this.processed = true;
		while(this.isRunning() || this.isPaused())
		{
			long time = System.currentTimeMillis();
			if(this.counter<0)
			{
				this.setResult(counter);
				this.throwError(new SampleError(42, "FOO!"));
			}
			log.info("... count  "+this.getName()+" to "+counter);
			if(this.counter>=this.max)
			{
				this.success();
			}
			this.checkAllInterrupts();
			this.counter++;
			this.setResult(counter);
			this.sleep(Math.max(0, 1000-(System.currentTimeMillis()-time)));
		}
	}

	@Override
	public void onError()
	{
		this.error = true;
		log.info("...on-error "+this.getName());
	}

	@Override
	public void onTimeout()
	{
		this.timedOut = true;
		log.info("...on-timeout "+this.getName());
	}

	@Override
	public void onAbort()
	{
		this.aborted = true;
		log.info("...on-abort "+this.getName());
	}

	public long getCounter()
	{
		return counter;
	}

	public boolean isPreProcessedFlagSet()
	{
		return preProcessed;
	}

	public boolean isPostProcessedFlagSet()
	{
		return postProcessed;
	}

	public boolean isProcessedFlagSet()
	{
		return processed;
	}

	public boolean isAbortFlagSet()
	{
		return aborted;
	}

	public boolean isTimeOutFlagSet()
	{
		return timedOut;
	}

	public boolean isErrorFlagSet()
	{
		return error;
	}
	
	public void setCounter(long counter)
	{
		this.counter = counter;
	}
	
	public String jobStats()
	{
		String s = "";
		s += "---\n";
		s += "JobName                : " + this.getName() + "\n";
		s += "JobDescription         : " + this.getDescription() + "\n";
		s += "SchedulerName          : " + this.getScheduler().getName() + "\n";
		s += "SchedulerDescription   : " + this.getScheduler().getDescription() + "\n";
		s += "---\n";
		s += "JobState               : " + this.getState() + "\n";
		s += "isFinished             : " + this.isFinished() + "\n";
		s += "JobError               : " + this.getError() + "\n";
		s += "---\n";
		s += "JobRuntime             : " + this.getRuntime() + "\n";
		s += "PreProcessingDuration  : " + this.getPreProcessingDuration() + "\n";
		s += "ProcessingDuration     : " + this.getProcessingDuration() + "\n";
		s += "PostProcessingDuration : " + this.getPostProcessingDuration() + "\n";
		s += "---\n";
		s += "Result                 : " + this.getResult() + "\n";
		s += "Counter                : " + this.getCounter() + "\n";
		s += "---\n";
		return s;
	}

	public SampleJob setPreProcessingDelay(long delay)
	{
		this.preProcessingDelay = delay;
		return this;
	}
	
	public SampleJob setPostProcessingDelay(long delay)
	{
		this.postProcessingDelay = delay;
		return this;
	}
	
}
