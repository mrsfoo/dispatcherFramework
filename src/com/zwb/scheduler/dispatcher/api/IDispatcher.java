package com.zwb.scheduler.dispatcher.api;

import java.util.List;

public interface IDispatcher<I, O>
{
	public IDispatcher<I, O> enqueue(I inputDataElement);
	public IDispatcher<I, O> enqueue(I... inputData);
	public IDispatcher<I, O> enqueue(List<I> inputData);
	
	public void chainAfterDispatcher(IDispatcher<?, I> dispatcher);
	
	public List<O> detachResults();
	public O detachResult();
	public int getResultCount();

	public void start();
	public void stop();
	
	public void pause();
	public void pauseAll();
	public void pause(long timeoutMillis);
	public void pauseAll(long timeoutMillis);
	
	public void resume();
	public void resumeAll();

	public void waitForResult();
	public boolean waitForResult(long timeoutMillis);
	public void waitForInputQueueEmpty();
	public boolean waitForInputQueueEmpty(long timeoutMillis);
	public void waitForIdle();
	//TODO
	public boolean waitForIdle(long timeoutMillis);
	
	public IDispatcher<I, O> setSchedulerSleepCycle(long sleepCycleMillis);
	public IDispatcher<I, O> setSchedulerMaxThread(int maxThread);
	public IDispatcher<I, O> setDispatcherJobSleepCycle(long sleepCycleMillis);
	public IDispatcher<I, O> setDispatcherSleepCycle(long sleepCycleMillis);
	
	public List<I> getInputQueueSnapshot();
	public List<O> getOutputQueueSnapshot();
	public List<I> getWorkingJobQueueSnapshot();
	
	public boolean isRunning();
	
	public void setName(String name);
	public String getName();
	public void setDescription(String desc);
	public String getDescription();
	
	
}
