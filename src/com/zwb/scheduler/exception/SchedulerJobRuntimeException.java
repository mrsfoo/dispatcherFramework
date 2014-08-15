package com.zwb.scheduler.exception;

public class SchedulerJobRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = -147292883682932544L;

	public SchedulerJobRuntimeException()
	{
		super();
	}
	
	public SchedulerJobRuntimeException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public SchedulerJobRuntimeException(String message)
	{
		super(message);
	}
	
	public SchedulerJobRuntimeException(Throwable cause)
	{
		super(cause);
	}
}
