package com.zwb.scheduler.exception;

public class SchedulerJobException extends Exception
{
	private static final long serialVersionUID = -7828198958334638253L;

	public SchedulerJobException()
	{
		super();
	}
	
	public SchedulerJobException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	public SchedulerJobException(String message)
	{
		super(message);
	}
	
	public SchedulerJobException(Throwable cause)
	{
		super(cause);
	}
}
