package com.zwb.scheduler.api;

public enum SchedulerJobState
{
	/** the job is created but hasn't been started yet. */
	INITALIZED,

	/** start request for the job was called but is in waiting queue for start. */
	WAITING,

	/** the job has been started and is running. */
	RUNNING,

	/** the job was run and has termiated. */
	SUCCESSFUL,

	/** the job has been aborted prematurely. */
	ABORTED,

	/** an error has occurred in job execution. */
	ERROR,

	/** a timeout has occurred in job execution. */
	TIMEOUT,

	/** the job has been paused. */
	PAUSED;
	
	public String toString(SchedulerJobState state)
	{
		switch(state)
		{
		case ABORTED:
			return "ABORTED";
		case ERROR:
			return "ERROR";
		case INITALIZED:
			return "INITALIZED";
		case PAUSED:
			return "PAUSED";
		case RUNNING:
			return "RUNNING";
		case SUCCESSFUL:
			return "SUCCESSFUL";
		case TIMEOUT:
			return "TIMEOUT";
		case WAITING:
			return "WAITING";
		default:
			return "UNKNOWN";
		}
	}
	
	public String toString()
	{
		return toString(this);
	}
}
