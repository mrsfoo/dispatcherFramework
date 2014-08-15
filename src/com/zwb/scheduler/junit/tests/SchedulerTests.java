package com.zwb.scheduler.junit.tests;

import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.junit.BasicSchedulerTestCase;
import com.zwb.scheduler.junit.util.SampleJob;
import com.zwb.scheduler.util.MyLogger;

public class SchedulerTests extends BasicSchedulerTestCase
{
	private static MyLogger log = new MyLogger(SchedulerTests.class);

	public void testSchedulerPause()
	{
		logStart(log, "starting testSchedulerPause");
		
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);
		this.scheduler.executeAsync(job3);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);		
		assertJobRunning(job2, JOB2_MAX);
		assertJobWaiting(job3, JOB3_MAX);
		
		this.scheduler.pause();
		
		job1.waitForFinishing();
		job2.waitForFinishing();
		
		assertJobSuccessful(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);
		assertJobWaiting(job3, JOB3_MAX);

		this.scheduler.resume();

		this.sleep(3000);
		assertJobRunning(job3, JOB3_MAX);		

		job3.waitForFinishing();		
		assertJobSuccessful(job3, JOB3_MAX);

		logEnd(log, "finished testSchedulerPause", job1, job2, job3);
	}

	public void testSchedulerPauseWithTimeout()
	{
		logStart(log, "starting testSchedulerPauseWithTimeout");

		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);
		this.scheduler.executeAsync(job3);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);		
		assertJobRunning(job2, JOB2_MAX);
		assertJobWaiting(job3, JOB3_MAX);
		
		this.scheduler.pause(10000);
		
		job1.waitForFinishing();
		job2.waitForFinishing();
		
		assertJobSuccessful(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);
		assertJobWaiting(job3, JOB3_MAX);

		this.sleep(4000);
		assertJobRunning(job3, JOB3_MAX);		

		job3.waitForFinishing();		
		assertJobSuccessful(job3, JOB3_MAX);

		logEnd(log, "finished testSchedulerPauseWithTimeout", job1, job2, job3);
	}

	public void testSchedulerWaitWithTimeout()
	{
		logStart(log, "starting testSchedulerWaitWithTimeout");
		long max = 30;
		job1 = new SampleJob(max);
		job2 = new SampleJob(max);
		job3 = new SampleJob(max);
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);
		this.scheduler.executeAsync(job3);
		
		this.sleep(2100);
		
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		long ts = System.currentTimeMillis();
		boolean timeout = this.scheduler.waitUntilNoJobsWaiting(2100);
		assertTrue(timeout);
		assertEqualsFuzzy(2100, System.currentTimeMillis()-ts, 0.35);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		ts = System.currentTimeMillis();
		timeout = this.scheduler.waitUntilNoJobsWaitingOrRunning(2100);
		assertTrue(timeout);
		assertEqualsFuzzy(2100, System.currentTimeMillis()-ts, 0.35);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		ts = System.currentTimeMillis();
		timeout = this.scheduler.waitUntilNoJobsRunning(2100);
		assertTrue(timeout);
		assertEqualsFuzzy(2100, System.currentTimeMillis()-ts, 0.35);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		ts = System.currentTimeMillis();
		timeout = this.scheduler.waitUntilWaitingJobCountIsBelow(0, 2100);
		assertTrue(timeout);
		assertEqualsFuzzy(2100, System.currentTimeMillis()-ts, 0.35);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		ts = System.currentTimeMillis();
		timeout = this.scheduler.waitUntilRunningJobCountIsBelow(0, 2100);
		assertTrue(timeout);
		assertEqualsFuzzy(2100, System.currentTimeMillis()-ts, 0.35);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		ts = System.currentTimeMillis();
		this.scheduler.waitUntilWaitingJobCountIsBelow(2);
		assertTrue((System.currentTimeMillis()-ts)<25);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		ts = System.currentTimeMillis();
		this.scheduler.waitUntilRunningJobCountIsBelow(3);
		assertTrue((System.currentTimeMillis()-ts)<25);
		assertJobRunning(job1, max);		
		assertJobRunning(job2, max);		
		assertJobWaiting(job3, max);
		
		this.scheduler.waitUntilNoJobsRunning();
		assertJobSuccessful(job1, max);		
		assertJobSuccessful(job2, max);		
		assertJobSuccessful(job3, max);		
		
		logEnd(log, "finished testSchedulerWaitWithTimeout", job1, job2, job3);
	}
	
	public void testSchedulerPauseAll()
	{
		logStart(log, "starting testSchedulerPauseAll");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);
		this.scheduler.executeAsync(job3);
		
		this.sleep(2100);
		
		assertJobRunning(job1, JOB1_MAX);		
		assertJobRunning(job2, JOB2_MAX);		
		assertJobWaiting(job3, JOB3_MAX);
		
		this.scheduler.pauseAll();
		
		job1.waitForStates(SchedulerJobState.PAUSED);
		job2.waitForStates(SchedulerJobState.PAUSED);

		assertJobPaused(job1, JOB1_MAX);		
		assertJobPaused(job2, JOB2_MAX);		
		assertJobWaiting(job3, JOB3_MAX);		
		
		this.scheduler.resumeAll();
		
		job1.waitForStates(SchedulerJobState.RUNNING);
		job2.waitForStates(SchedulerJobState.RUNNING);

		assertJobRunningAfterPause(job1, JOB1_MAX);		
		assertJobRunningAfterPause(job2, JOB2_MAX);		
		assertJobWaiting(job3, JOB3_MAX);

		this.scheduler.pauseAll(3000);

		job1.waitForStates(SchedulerJobState.PAUSED);
		job2.waitForStates(SchedulerJobState.PAUSED);

		assertJobPaused(job1, JOB1_MAX);		
		assertJobPaused(job2, JOB2_MAX);		
		assertJobWaiting(job3, JOB3_MAX);
		
		this.sleep(4500);
		
		assertJobRunningAfterPause(job1, JOB1_MAX);		
		assertJobRunningAfterPause(job2, JOB2_MAX);		
		assertJobWaiting(job3, JOB3_MAX);

		this.scheduler.waitUntilNoJobsWaitingOrRunning();
		
		assertJobSuccessfulAfterPause(job1, JOB1_MAX);		
		assertJobSuccessfulAfterPause(job2, JOB2_MAX);		
		assertJobSuccessfulAfterPause(job3, JOB3_MAX);		
		
		logEnd(log, "finished testSchedulerPauseAll", job1, job2, job3);
	}
}
