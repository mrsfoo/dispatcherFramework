package com.zwb.scheduler.dispatcher.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zwb.scheduler.api.Defaults;
import com.zwb.scheduler.api.SchedulerFactory;
import com.zwb.scheduler.dispatcher.api.IDispatcher;
import com.zwb.scheduler.dispatcher.api.IDispatcherWorkerJob;
import com.zwb.scheduler.exception.SchedulerJobRuntimeException;
import com.zwb.scheduler.impl.Scheduler;
import com.zwb.scheduler.util.MyLogger;

public class Dispatcher<I, O> implements IDispatcher<I, O>
{
	List<I> inputQueue = new ArrayList<I>();
	List<O> outputQueue = new ArrayList<O>();
	Scheduler scheduler;
	DispatcherJob<I, O> dispatcherJob;

	Class<I> inputDataClass;
	Class<O> outputDataClass;
	Class<? extends IDispatcherWorkerJob<I, O>> jobWorkerClass;

	long schedulerSleepCycle = Defaults.SCHEDULER_SLEEP_CYCLE;
	int schedulerMaxThead = Defaults.SCHEDULER_MAX_THREADS;
	long dispatcherJobSleepCycle = Defaults.SCHEDULER_JOB_SLEEP_CYCLE;
	long dispatcherSleepCycle = Defaults.DISPATCHER_SLEEP_CYCLE;

	private static MyLogger log = new MyLogger(Dispatcher.class);

	String name = "";
	String description = "";
	
	public Dispatcher(Class<I> inputDataClass, Class<O> outputDataClass, Class<? extends IDispatcherWorkerJob<I, O>> jobClass)
	{
		log.debug("creating dispatcher for inputDataClass: ", inputDataClass, "; outputDataClass: ", outputDataClass, "; workerJobClass: ", jobClass);
		this.inputDataClass = inputDataClass;
		this.outputDataClass = outputDataClass;
		this.jobWorkerClass = jobClass;

		this.scheduler = (Scheduler) SchedulerFactory.createScheduler();
		this.scheduler.setDescription("Dispatcher for Types [I:" + this.inputDataClass + " -> O:" + this.outputDataClass + "]");
		this.scheduler.setMaxThread(schedulerMaxThead);
		this.scheduler.setName("Dispatcher");
		this.scheduler.setSleepCycle(schedulerSleepCycle);

		this.dispatcherJob = new DispatcherJob<I, O>(this);
		this.dispatcherJob.setDescription("Job for dispatching input data");
		this.dispatcherJob.setName("DispatcherJob");
		this.dispatcherJob.setSleepCycle(dispatcherJobSleepCycle);
		
		this.setName("Dispatcher [I:" + this.inputDataClass + " -> O:" + this.outputDataClass + "]");
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setDescription(String desc)
	{
		this.description = desc;
	}
	
	public String getDescription()
	{
		return this.description;
	}

	public List<I> getInputQueue()
	{
		return inputQueue;
	}

	public void setInputQueue(List<I> inputQueue)
	{
		synchronized (this.inputQueue)
		{
			this.inputQueue = inputQueue;
		}
	}

	public List<O> getOutputQueue()
	{
		return outputQueue;
	}

	public void setOutputQueue(List<O> outputQueue)
	{
		synchronized (this.outputQueue)
		{
			this.outputQueue = outputQueue;
		}
	}

	public void addToOutputQueue(O... outputData)
	{
		synchronized (this.outputQueue)
		{
			for (O o : outputData)
			{
				this.outputQueue.add(o);
			}
		}
	}

	public void addToOutputQueue(List<O> outputData)
	{
		synchronized (this.outputQueue)
		{
			this.outputQueue.addAll(outputData);
		}
	}

	@Override
	public IDispatcher<I, O> setSchedulerSleepCycle(long sleepCycleMillis)
	{
		this.schedulerSleepCycle = sleepCycleMillis;
		this.scheduler.setSleepCycle(sleepCycleMillis);
		return this;
	}

	@Override
	public IDispatcher<I, O> setSchedulerMaxThread(int maxThread)
	{
		this.schedulerMaxThead = maxThread;
		this.scheduler.setMaxThread(maxThread);
		return this;
	}

	@Override
	public IDispatcher<I, O> setDispatcherJobSleepCycle(long sleepCycleMillis)
	{
		this.dispatcherJobSleepCycle = sleepCycleMillis;
		this.dispatcherJob.setSleepCycle(sleepCycleMillis);
		return this;
	}

	@Override
	public IDispatcher<I, O> setDispatcherSleepCycle(long sleepCycleMillis)
	{
		this.dispatcherSleepCycle = sleepCycleMillis;
		return this;
	}

	@Override
	public IDispatcher<I, O> enqueue(I inputDataElement)
	{
		log.trace("enqueue input data: ", inputDataElement);
		synchronized (this.inputQueue)
		{
			this.inputQueue.add(inputDataElement);
		}
		return this;
	}

	@Override
	public IDispatcher<I, O> enqueue(I... inputData)
	{
		synchronized (this.inputQueue)
		{
			for (I i : inputData)
			{
				log.trace("enqueue input data: ", i);
				this.inputQueue.add(i);
			}
		}
		return this;
	}

	@Override
	public IDispatcher<I, O> enqueue(List<I> inputData)
	{
		synchronized (this.inputQueue)
		{
			for (I i : inputData)
			{
				log.trace("enqueue input data: ", i);
				this.inputQueue.add(i);
			}
		}
		return this;
	}

	@Override
	public void chainAfterDispatcher(IDispatcher<?, I> previousDispatcher)
	{
		log.trace("building dispatcher chain: ", previousDispatcher.getName(), " >>> ", this.getName());
		synchronized (this.inputQueue)
		{
			this.setInputQueue(((Dispatcher) previousDispatcher).getOutputQueue());
		}
	}

	@Override
	public List<O> detachResults()
	{
		List<O> results = new ArrayList<O>();
		synchronized (this.outputQueue)
		{
			for (int i = 0; i < this.outputQueue.size(); i++)
			{
				results.add(this.detachResult());
			}
		}
		return results;
	}

	@Override
	public O detachResult()
	{
		log.trace("detachResult");
		synchronized (this.outputQueue)
		{
			if (this.outputQueue.isEmpty())
			{
				return null;
			}
			return this.outputQueue.remove(0);
		}
	}

	@Override
	public int getResultCount()
	{
		synchronized (this.outputQueue)
		{
			return this.outputQueue.size();
		}
	}

	public List<I> detachInputDataElements()
	{
		List<I> results = new ArrayList<I>();
		synchronized (this.inputQueue)
		{
			for (int i = 0; i < this.inputQueue.size(); i++)
			{
				results.add(this.detachInputDataElement());
			}
		}
		return results;
	}

	public I detachInputDataElement()
	{
		log.trace("detachInputDataElement");
		synchronized (this.inputQueue)
		{
			if (this.inputQueue.isEmpty())
			{
				return null;
			}
			return this.inputQueue.remove(0);
		}
	}

	public int getInputQueueSize()
	{
		synchronized (this.inputQueue)
		{
			return this.inputQueue.size();
		}
	}

	@Override
	public void start()
	{
		log.trace("starting dispatcher: ", this.getName());
		this.scheduler.start();
		this.scheduler.executeAsync(this.dispatcherJob);
	}

	@Override
	public void stop()
	{
		log.trace("stopping dispatcher: ", this.getName());
		this.dispatcherJob.abortAsync();
		this.scheduler.stop();
	}

	@Override
	public void pause()
	{
		log.trace("pausing dispatcher: ", this.getName());
		this.dispatcherJob.pauseAsync();
	}

	@Override
	public void pauseAll()
	{
		log.trace("pausing dispatcher and all jobs: ", this.getName());
		this.dispatcherJob.pauseAsync();
		this.scheduler.pauseAll();
	}

	@Override
	public void pause(long timeoutMillis)
	{
		log.trace("pausing dispatcher ", this.getName(), "with pause timeout ", timeoutMillis, "ms");
		this.dispatcherJob.pauseAsync(timeoutMillis);
	}

	@Override
	public void pauseAll(long timeoutMillis)
	{
		log.trace("pausing dispatcher and all jobs ", this.getName(), "with pause timeout ", timeoutMillis, "ms");
		this.dispatcherJob.pauseAsync(timeoutMillis);
		this.scheduler.pauseAll(timeoutMillis);
	}

	@Override
	public void resume()
	{
		log.trace("resuming dispatcher: ", this.getName());
		this.dispatcherJob.resumeAsync();
	}

	@Override
	public void resumeAll()
	{
		log.trace("resuming dispatcher and all jobs: ", this.getName());
		this.dispatcherJob.resumeAsync();
		this.scheduler.resumeAll();
	}

	@Override
	public void waitForResult()
	{
		waitForResult(-42);
	}

	@Override
	public boolean waitForResult(long timeoutMillis)
	{
		long timestampWaitStarted = System.currentTimeMillis();
		while (true)
		{
			if ((timeoutMillis >= 0) && (System.currentTimeMillis() - timestampWaitStarted) > timeoutMillis)
			{
				return true;
			}
			if (!this.outputQueue.isEmpty())
			{
				return false;
			}
			this.sleep(this.dispatcherSleepCycle);
		}
	}

	public void waitForInputQueueEmpty()
	{
		waitForInputQueueEmpty(-42);
	}
	
	public boolean waitForInputQueueEmpty(long timeoutMillis)
	{
		long timestampWaitStarted = System.currentTimeMillis();
		while (true)
		{
			if ((timeoutMillis >= 0) && (System.currentTimeMillis() - timestampWaitStarted) > timeoutMillis)
			{
				return true;
			}
			if (this.inputQueue.isEmpty())
			{
				return false;
			}
			this.sleep(this.dispatcherSleepCycle);
		}
	}

	public void waitForIdle()
	{
		waitForIdle(-42);
	}
	
	//TODO
	public boolean waitForIdle(long timeoutMillis)
	{
		long timestampWaitStarted = System.currentTimeMillis();
		while (true)
		{
			if ((timeoutMillis >= 0) && (System.currentTimeMillis() - timestampWaitStarted) > timeoutMillis)
			{
				return true;
			}
			this.scheduler.waitUntilRunningJobCountIsBelow(2, timeoutMillis);
			this.scheduler.waitUntilWaitingJobCountIsBelow(2, timeoutMillis);
			if (this.inputQueue.isEmpty())
			{
				return false;
			}
			this.sleep(this.dispatcherSleepCycle);
		}
	}

	private final void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			new SchedulerJobRuntimeException("sleep interrupted");
		}
	}

	public IDispatcherWorkerJob<I, O> createNewWorkerJob()
	{
		log.debug("create new worker job of type: ", this.jobWorkerClass);
		try
		{
			IDispatcherWorkerJob<I, O> workerJob = this.jobWorkerClass.newInstance();
			return workerJob;
		}
		catch (InstantiationException e)
		{
			throw new SchedulerJobRuntimeException("InstantiationException while creating new worker job", e);
		}
		catch (IllegalAccessException e)
		{
			throw new SchedulerJobRuntimeException("IllegalAccessException while creating new worker job", e);
		}
	}

	@Override
	public List<I> getInputQueueSnapshot()
	{
		synchronized(this.inputQueue)
		{
			return Collections.unmodifiableList(new ArrayList<I>(this.inputQueue));
		}
	}

	@Override
	public List<O> getOutputQueueSnapshot()
	{
		synchronized(this.outputQueue)
		{
			return Collections.unmodifiableList(new ArrayList<O>(this.outputQueue));
		}
	}

	@Override
	public List<I> getWorkingJobQueueSnapshot()
	{
		return Collections.unmodifiableList(new ArrayList<I>(this.dispatcherJob.getWorkingSnapshot()));
	}

	@Override
	public boolean isRunning()
	{
		return this.scheduler.isRunning();
	}

}
