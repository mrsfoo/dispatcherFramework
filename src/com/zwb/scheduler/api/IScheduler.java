package com.zwb.scheduler.api;

public interface IScheduler
{
	public IScheduler setMaxThread(int max);
	public IScheduler setSleepCycle(long sleepCycleMillis);
	public void start();
	public void stop();

	public void pause();
	public void pause(long timeoutMillis);
	public void pauseAll();
	public void pauseAll(long timeoutMillis);
	public void resume();
	public void resumeAll();
	
	public IScheduler setDescription(String description);
	public IScheduler setName(String name);
	public String getDescription();
	public String getName();
	
	public <T> T executeSync(ISchedulerJob<T> job);
	public void executeAsync(ISchedulerJob<?> job);
	
	public void waitUntilWaitingJobCountIsBelow(int jobCount);
	public void waitUntilRunningJobCountIsBelow(int jobCount);
	public void waitUntilNoJobsWaiting();
	public void waitUntilNoJobsRunning();
	public void waitUntilNoJobsWaitingOrRunning();
	public boolean waitUntilWaitingJobCountIsBelow(int jobCount, long timeoutMillis);
	public boolean waitUntilRunningJobCountIsBelow(int jobCount, long timeoutMillis);
	public boolean waitUntilNoJobsWaiting(long timeoutMillis);
	public boolean waitUntilNoJobsRunning(long timeoutMillis);
	public boolean waitUntilNoJobsWaitingOrRunning(long timeoutMillis);
	
	public boolean isRunning();
}
