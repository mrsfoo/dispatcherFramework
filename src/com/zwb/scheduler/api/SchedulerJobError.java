package com.zwb.scheduler.api;

public abstract class SchedulerJobError
{
	int errorId = 0;
	String errorMessage = "";
	String callingThreadName = "";
	long errorTimestamp = 0;
	
	public SchedulerJobError(int errorId, String errorMessage, long timestampMillis)
	{
		this.errorId = errorId;
		this.errorMessage = errorMessage;
		this.errorTimestamp = timestampMillis;
		Thread.currentThread().getName();
	}
	
	public SchedulerJobError(int errorId, String errorMessage)
	{
		this(errorId, errorMessage, System.currentTimeMillis());
	}
	
}
