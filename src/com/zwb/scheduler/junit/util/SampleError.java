package com.zwb.scheduler.junit.util;

import com.zwb.scheduler.api.SchedulerJobError;

public class SampleError extends SchedulerJobError
{

	public SampleError(int errorId, String errorMessage)
	{
		super(errorId, errorMessage);
	}

}
