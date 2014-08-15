package com.zwb.scheduler.junit.util;

public class LoadDataSet
{
	int noOfJobsRunning = 0;
	int noOfJobsWaiting = 0;
	long timestamp;
	
	public LoadDataSet(long startingTimestamp, int noOfJobsWaiting, int noOfJobsRunning)
	{
		this.timestamp = System.currentTimeMillis()-startingTimestamp;
		this.noOfJobsRunning = noOfJobsRunning;
		this.noOfJobsWaiting = noOfJobsWaiting;
	}
}