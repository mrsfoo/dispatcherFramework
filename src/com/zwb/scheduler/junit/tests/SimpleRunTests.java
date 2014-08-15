package com.zwb.scheduler.junit.tests;

import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.junit.BasicSchedulerTestCase;
import com.zwb.scheduler.util.MyLogger;

public class SimpleRunTests extends BasicSchedulerTestCase
{
	private static MyLogger log = new MyLogger(SimpleRunTests.class);

	public void testExecuteSyncSingleJob()
	{
		logStart(log, "starting testExecuteSyncSingleJob");
		long result = this.scheduler.executeSync(job1);

		assertEquals(JOB1_MAX, result);
		assertJobSuccessful(job1, JOB1_MAX);

		log.info(job1.jobStats());
		logEnd(log, "finished testExecuteSyncSingleJob", job1);
	}

	public void testExecuteAsyncDualJobs()
	{
		logStart(log, "starting testExecuteAsyncDualJobs");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);
		
		long cnt = job1.getCounter();
		this.scheduler.executeAsync(job1);
		assertJobRunning(job1, JOB1_MAX);
		this.sleep(1200);
		assertTrue(job1.getCounter()>cnt);
		
		job1.waitForFinishing();
		job2.waitForFinishing();
		
		assertJobSuccessful(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		log.info(job1.jobStats());
		log.info(job2.jobStats());
		logEnd(log, "finished testExecuteAsyncDualJobs", job1, job2);
	}

	public void testExecuteAsyncTripleJobsWaiting()
	{
		logStart(log, "starting testExecuteAsyncTripleJobsWaiting");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);
		this.scheduler.executeAsync(job3);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);
		assertJobWaiting(job3, JOB3_MAX);
		
		job1.waitForFinishing();
		job2.waitForFinishing();
		this.sleep(2000);
		
		assertJobSuccessful(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);
		assertJobRunning(job3, JOB3_MAX);

		job3.waitForFinishing();
		assertJobSuccessful(job3, JOB3_MAX);

		log.info(job1.jobStats());
		log.info(job2.jobStats());
		log.info(job3.jobStats());
		logEnd(log, "finished testExecuteAsyncTripleJobsWaiting", job1, job2, job3);
	}
	
	public void testWaitWithTimeout()
	{
		logStart(log, "starting testWaitWithTimeout");
		this.scheduler.executeAsync(job1);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);

		long timestamp = System.currentTimeMillis();
		boolean timeout = job1.waitForStates(5000, SchedulerJobState.PAUSED);
		assertTrue(timeout);
		assertJobRunning(job1, JOB1_MAX);
		assertTrue((System.currentTimeMillis()-timestamp)>=5000);
		
		job1.waitForFinishing();
		
		assertJobSuccessful(job1, JOB1_MAX);

		log.info(job1.jobStats());
		logEnd(log, "finished testWaitWithTimeout", job1);
	}
}
