package com.zwb.scheduler.junit.tests;

import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.junit.BasicSchedulerTestCase;
import com.zwb.scheduler.util.MyLogger;

public class InterruptTests extends BasicSchedulerTestCase
{
	private static MyLogger log = new MyLogger(InterruptTests.class);

	public void testJobAbortSync()
	{
		logStart(log, "starting testJobAbortSync");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.abortSync();
		assertJobAborted(job1, JOB1_MAX);
		job2.waitForFinishing();
		
		assertJobAborted(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		job1.pauseAsync();
		assertJobAborted(job1, JOB1_MAX);
		
		this.scheduler.executeAsync(job1);
		assertJobAborted(job1, JOB1_MAX);
		
		logEnd(log, "finished testJobAbortSync", job1, job2);
	}

	public void testJobAbortAsync()
	{
		logStart(log, "starting testJobAbortAsync");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.abortAsync();
		this.sleep(1500);
		assertJobAborted(job1, JOB1_MAX);

		long time = System.currentTimeMillis();
		job1.waitForFinishing();
		assertTrue((System.currentTimeMillis()-time)<10);
		
		job2.waitForFinishing();
		
		assertJobAborted(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		logEnd(log, "finished testJobAbortAsync", job1, job2);
	}

	public void testJobAbortInPause()
	{
		logStart(log, "starting testJobAbortInPause");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.pauseSync();
		assertJobPaused(job1, JOB1_MAX);
		
		job1.abortSync();
		assertJobAborted(job1, JOB1_MAX);

		long time = System.currentTimeMillis();
		job1.waitForFinishing();
		assertTrue((System.currentTimeMillis()-time)<10);
		
		job2.waitForFinishing();
		
		assertJobAborted(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		logEnd(log, "finished testJobAbortInPause", job1, job2);
	}

	public void testJobPauseSync()
	{
		logStart(log, "starting testJobPauseSync");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.pauseSync();
		
		assertJobRunning(job2, JOB2_MAX);
		assertJobPaused(job1, JOB1_MAX);
		
		job2.waitForFinishing();

		assertJobPaused(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		this.scheduler.executeAsync(job1);
		assertJobPaused(job1, JOB1_MAX);
		
		job1.resumeSync();

		assertJobRunningAfterPause(job1, JOB1_MAX);
		
		job1.waitForFinishing();

		assertJobSuccessfulAfterPause(job1, JOB1_MAX);

		logEnd(log, "finished testJobPauseSync", job1, job2);
	}

	public void testJobPauseAsync()
	{
		logStart(log, "starting testJobPauseAsync");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.pauseAsync();
		job1.waitForStates(SchedulerJobState.PAUSED);		
		
		assertJobRunning(job2, JOB2_MAX);
		assertJobPaused(job1, JOB1_MAX);
		
		job2.waitForFinishing();

		assertJobPaused(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		job1.resumeAsync();
		job1.waitForStates(SchedulerJobState.RUNNING);		

		assertJobRunningAfterPause(job1, JOB1_MAX);
		
		job1.waitForFinishing();

		assertJobSuccessfulAfterPause(job1, JOB1_MAX);

		logEnd(log, "finished testJobPauseAsync", job1, job2);
	}

	public void testJobPauseWithTimeout()
	{
		logStart(log, "starting testJobPauseWithTimeout");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);
		
		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);
		
		job1.pauseSync(5000);
		
		assertJobRunning(job2, JOB2_MAX);
		assertJobPaused(job1, JOB1_MAX);
		
		this.sleep(6000);
		assertJobRunningAfterPause(job1, JOB1_MAX);

		job2.waitForFinishing();
		assertJobSuccessful(job2, JOB2_MAX);

		job1.waitForFinishing();
		assertJobSuccessfulAfterPause(job1, JOB1_MAX);

		logEnd(log, "finished testJobPauseWithTimeout", job1, job2);
	}

	public void testJobTimeout()
	{		
		logStart(log, "starting testJobTimeout");
		job1.setTimeout(7500);
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.waitForFinishing();
		job2.waitForFinishing();
		
		assertJobTimeout(job1, JOB1_MAX, 7500);
		assertJobSuccessful(job2, JOB2_MAX);

		job1.abortAsync();
		assertJobTimeout(job1, JOB1_MAX, 7500);
		
		job1.pauseAsync();
		assertJobTimeout(job1, JOB1_MAX, 7500);
		
		this.scheduler.executeAsync(job1);
		assertJobTimeout(job1, JOB1_MAX, 7500);
		
		logEnd(log, "finished testJobTimeout", job1, job2);
	}

	public void testJobTimeoutInPause()
	{		
		logStart(log, "starting testJobTimeout");
		job1.setTimeout(7500);
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.pauseSync();
		assertJobPaused(job1, JOB1_MAX);
		
		job1.waitForFinishing();
		job2.waitForFinishing();
		
		assertStatesAndFlagsJobTimeout(job1);

		assertEqualsFuzzy(3, job1.getCounter(), 0.4);
		assertEqualsFuzzy(7500, job1.getRuntime(), DEVIATION);
		assertEqualsFuzzy(7500, job1.getProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(420, job1.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job1.getPostProcessingDuration());
		
		assertJobSuccessful(job2, JOB2_MAX);

		logEnd(log, "finished testJobTimeout", job1, job2);
	}
	
	public void testJobError()
	{
		logStart(log, "starting testJobError");
		this.scheduler.executeAsync(job1);
		this.scheduler.executeAsync(job2);

		this.sleep(3000);
		assertJobRunning(job1, JOB1_MAX);
		assertJobRunning(job2, JOB2_MAX);

		job1.setCounter(-42);
		this.sleep(1500);
		assertJobError(job1, JOB1_MAX);

		job1.abortAsync();
		assertJobError(job1, JOB1_MAX);
		
		job1.pauseAsync();
		assertJobError(job1, JOB1_MAX);
		
		this.scheduler.executeAsync(job1);
		assertJobError(job1, JOB1_MAX);
		
		long time = System.currentTimeMillis();
		job1.waitForFinishing();
		assertTrue((System.currentTimeMillis()-time)<25);
		
		job2.waitForFinishing();
		
		assertJobError(job1, JOB1_MAX);
		assertJobSuccessful(job2, JOB2_MAX);

		logEnd(log, "finished testJobError", job1, job2);
	}

}
