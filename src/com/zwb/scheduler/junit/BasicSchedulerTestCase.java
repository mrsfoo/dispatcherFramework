package com.zwb.scheduler.junit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.zwb.scheduler.api.IScheduler;
import com.zwb.scheduler.api.SchedulerFactory;
import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.junit.util.SampleError;
import com.zwb.scheduler.junit.util.SampleJob;
import com.zwb.scheduler.util.MyLogger;

public class BasicSchedulerTestCase extends TestCase
{
	protected IScheduler scheduler;
	protected SampleJob job1;
	protected SampleJob job2;
	protected SampleJob job3;

	protected final int JOB1_MAX = 10;
	protected final int JOB2_MAX = 10;
	protected final int JOB3_MAX = 10;
	protected final double DEVIATION = 0.25;
	
	protected final long jobSleepCycle = 200;
	protected final long schedulerSleepCycle = 100;
	
	private static MyLogger log = new MyLogger(BasicSchedulerTestCase.class);

	@Override
	public void setUp()
	{
		log.info("");
		log.info("starting test setUp");

		scheduler = SchedulerFactory.createScheduler();
		scheduler.setName("SampleScheduler");
		scheduler.setMaxThread(2);
		scheduler.setSleepCycle(schedulerSleepCycle);
		scheduler.start();
		
		job1 = new SampleJob(JOB1_MAX);
		job1.setName("SampleJob1");
		job1.setSleepCycle(jobSleepCycle);
		assertFalse(job1.isAborted());
		assertFalse(job1.isError());
		assertFalse(job1.isFinished());
		assertTrue(job1.isInitialized());
		assertFalse(job1.isPaused());
		assertFalse(job1.isRunning());
		assertFalse(job1.isSuccessful());
		assertFalse(job1.isTimeout());

		job2 = new SampleJob(JOB2_MAX);
		job2.setName("SampleJob2");
		job2.setSleepCycle(jobSleepCycle);

		job3 = new SampleJob(JOB3_MAX);
		job3.setName("SampleJob3");
		job3.setSleepCycle(jobSleepCycle);

		log.info("finished test setUp");
		log.info("");
	}
	
	@Override
	public void tearDown()
	{
		log.info("");
		log.info("starting test tearDown");
		this.scheduler.stop();
		log.info("finished test tearDown");
		log.info("");
	}
	
	public void assertEqualsFuzzy(double expected, double actual, double deviation)
	{
		assertEquals(expected, actual, (expected*deviation));
	}
	
	public void assertStatesAndFlagsJobWaiting(SampleJob job)
	{
		assertEquals(SchedulerJobState.WAITING, job.getState());
		assertEquals(null, job.getError());

		assertFalse(job.isPostProcessedFlagSet());
		assertFalse(job.isAbortFlagSet());
		assertFalse(job.isErrorFlagSet());
		assertFalse(job.isPreProcessedFlagSet());
		assertFalse(job.isProcessedFlagSet());
		assertFalse(job.isTimeOutFlagSet());

		assertFalse(job.isAborted());
		assertFalse(job.isError());
		assertFalse(job.isFinished());
		assertFalse(job.isInitialized());
		assertFalse(job.isPaused());
		assertFalse(job.isRunning());
		assertFalse(job.isSuccessful());
		assertFalse(job.isTimeout());
		assertTrue(job.isWaiting());
	}
	
	public void assertStatesAndFlagsJobSuccessful(SampleJob job)
	{
		assertEquals(SchedulerJobState.SUCCESSFUL, job.getState());
		assertEquals(null, job.getError());

		assertTrue(job.isPostProcessedFlagSet());
		assertFalse(job.isAbortFlagSet());
		assertFalse(job.isErrorFlagSet());
		assertTrue(job.isPreProcessedFlagSet());
		assertTrue(job.isProcessedFlagSet());
		assertFalse(job.isTimeOutFlagSet());

		assertFalse(job.isAborted());
		assertFalse(job.isError());
		assertTrue(job.isFinished());
		assertFalse(job.isInitialized());
		assertFalse(job.isPaused());
		assertFalse(job.isRunning());
		assertTrue(job.isSuccessful());
		assertFalse(job.isTimeout());
		assertFalse(job.isWaiting());
	}
	
	public void assertStatesAndFlagsJobAborted(SampleJob job)
	{
		assertEquals(SchedulerJobState.ABORTED, job.getState());
		assertEquals(null, job.getError());

		assertFalse(job.isPostProcessedFlagSet());
		assertTrue(job.isAbortFlagSet());
		assertFalse(job.isErrorFlagSet());
		assertTrue(job.isPreProcessedFlagSet());
		assertTrue(job.isProcessedFlagSet());
		assertFalse(job.isTimeOutFlagSet());

		assertTrue(job.isAborted());
		assertFalse(job.isError());
		assertTrue(job.isFinished());
		assertFalse(job.isInitialized());
		assertFalse(job.isPaused());
		assertFalse(job.isRunning());
		assertFalse(job.isSuccessful());
		assertFalse(job.isTimeout());
		assertFalse(job.isWaiting());
	}
	
	public void assertStatesAndFlagsJobError(SampleJob job)
	{
		assertEquals(SchedulerJobState.ERROR, job.getState());
		assertNotNull(job.getError());
		assertEquals(SampleError.class, job.getError().getClass());
		
		assertFalse(job.isPostProcessedFlagSet());
		assertFalse(job.isAbortFlagSet());
		assertTrue(job.isErrorFlagSet());
		assertTrue(job.isPreProcessedFlagSet());
		assertTrue(job.isProcessedFlagSet());
		assertFalse(job.isTimeOutFlagSet());

		assertFalse(job.isAborted());
		assertTrue(job.isError());
		assertTrue(job.isFinished());
		assertFalse(job.isInitialized());
		assertFalse(job.isPaused());
		assertFalse(job.isRunning());
		assertFalse(job.isSuccessful());
		assertFalse(job.isTimeout());
		assertFalse(job.isWaiting());
	}
	
	public void assertStatesAndFlagsJobRunning(SampleJob job)
	{
		assertEquals(SchedulerJobState.RUNNING, job.getState());
		assertEquals(null, job.getError());

		assertFalse(job.isPostProcessedFlagSet());
		assertFalse(job.isAbortFlagSet());
		assertFalse(job.isErrorFlagSet());
		assertTrue(job.isPreProcessedFlagSet());
		assertTrue(job.isProcessedFlagSet());
		assertFalse(job.isTimeOutFlagSet());

		assertFalse(job.isAborted());
		assertFalse(job.isError());
		assertFalse(job.isFinished());
		assertFalse(job.isInitialized());
		assertFalse(job.isPaused());
		assertTrue(job.isRunning());
		assertFalse(job.isSuccessful());
		assertFalse(job.isTimeout());
		assertFalse(job.isWaiting());
	}
	
	public void assertStatesAndFlagsJobTimeout(SampleJob job)
	{
		assertEquals(SchedulerJobState.TIMEOUT, job.getState());
		assertEquals(null, job.getError());

		assertFalse(job.isPostProcessedFlagSet());
		assertFalse(job.isAbortFlagSet());
		assertFalse(job.isErrorFlagSet());
		assertTrue(job.isPreProcessedFlagSet());
		assertTrue(job.isProcessedFlagSet());
		assertTrue(job.isTimeOutFlagSet());

		assertFalse(job.isAborted());
		assertFalse(job.isError());
		assertTrue(job.isFinished());
		assertFalse(job.isInitialized());
		assertFalse(job.isPaused());
		assertFalse(job.isRunning());
		assertFalse(job.isSuccessful());
		assertTrue(job.isTimeout());
		assertFalse(job.isWaiting());
	}
	
	public void assertStatesAndFlagsJobPaused(SampleJob job)
	{
		assertEquals(SchedulerJobState.PAUSED, job.getState());
		assertEquals(null, job.getError());

		assertFalse(job.isPostProcessedFlagSet());
		assertFalse(job.isAbortFlagSet());
		assertFalse(job.isErrorFlagSet());
		assertTrue(job.isPreProcessedFlagSet());
		assertTrue(job.isProcessedFlagSet());
		assertFalse(job.isTimeOutFlagSet());

		assertFalse(job.isAborted());
		assertFalse(job.isError());
		assertFalse(job.isFinished());
		assertFalse(job.isInitialized());
		assertTrue(job.isPaused());
		assertFalse(job.isRunning());
		assertFalse(job.isSuccessful());
		assertFalse(job.isTimeout());
		assertFalse(job.isWaiting());
	}
	
	public void assertCountersAndDurationsJobWaiting(SampleJob job, long maxCounter)
	{
		assertEquals(null, job.getResult());
		assertEquals(0, job.getCounter());
		assertEqualsFuzzy(0, job.getProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(0, job.getPreProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(0, job.getPostProcessingDuration(), DEVIATION);
		assertEquals(0, job.getRuntime());
	}
	
	public void assertCountersAndDurationsJobAborted(SampleJob job, long maxCounter)
	{
		assertTrue(maxCounter>job.getResult().longValue());
		assertTrue(maxCounter>job.getCounter());		
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job.getPostProcessingDuration());
		assertTrue(job.getRuntime()<maxCounter*1000);
	}
	
	public void assertCountersAndDurationsJobSuccessful(SampleJob job, long maxCounter)
	{
		assertEquals(maxCounter, job.getResult().longValue());
		assertEquals(maxCounter, job.getCounter());
		assertEqualsFuzzy(maxCounter*1000, job.getProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(240, job.getPostProcessingDuration(), DEVIATION);
		assertEquals(job.getProcessingDuration(), job.getRuntime());
	}
	
	public void assertCountersAndDurationsJobSuccessfulAfterPause(SampleJob job, long maxCounter)
	{
		assertEquals(maxCounter, job.getResult().longValue());
		assertEquals(maxCounter, job.getCounter());
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(240, job.getPostProcessingDuration(), DEVIATION);
		assertEquals(job.getProcessingDuration(), job.getRuntime());
	}
	
	public void assertCountersAndDurationsJobRunning(SampleJob job, long maxCounter)
	{
		assertTrue(maxCounter>job.getResult().longValue());
		assertTrue(maxCounter>job.getCounter());		
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job.getPostProcessingDuration());
		assertTrue(job.getRuntime()<maxCounter*1000);

		long r1 = job.getRuntime();
		this.sleep(30);
		long r2 = job.getRuntime();
		assertTrue(r2>r1);
	}
	
	public void assertCountersAndDurationsJobRunningAfterPause(SampleJob job, long maxCounter)
	{
		assertTrue(maxCounter>job.getResult().longValue());
		assertTrue(maxCounter>job.getCounter());		
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job.getPostProcessingDuration());

		long r1 = job.getRuntime();
		this.sleep(30);
		long r2 = job.getRuntime();
		assertTrue(r2>r1);
	}
	
	public void assertCountersAndDurationsJobError(SampleJob job, long maxCounter)
	{
		assertTrue(job.getResult().longValue()<0);
		assertTrue(job.getCounter()<0);		
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job.getPostProcessingDuration());
		assertTrue(job.getRuntime()<maxCounter*1000);
	}
	
	public void assertCountersAndDurationsJobTimeout(SampleJob job, long maxCounter, long timeout)
	{
		assertTrue(maxCounter>job.getResult().longValue());
		assertTrue(maxCounter>job.getCounter());		
		assertEqualsFuzzy(timeout, job.getCounter()*1000, DEVIATION);
		assertEqualsFuzzy(timeout, job.getRuntime(), DEVIATION);
		assertEqualsFuzzy(timeout, job.getProcessingDuration(), DEVIATION);
		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job.getPostProcessingDuration());
		assertTrue(job.getRuntime()<maxCounter*1000);
	}	
	
	public void assertCountersAndDurationsJobPaused(SampleJob job, long maxCounter)
	{
		long c11 = job.getResult().longValue();
		long c12 = job.getCounter();
		this.sleep(1000);
		long c21 = job.getResult().longValue();
		long c22 = job.getCounter();
		
		assertEquals(c11, c21);
		assertEquals(c12, c22);

		assertEqualsFuzzy(420, job.getPreProcessingDuration(), DEVIATION);
		assertEquals(0, job.getPostProcessingDuration());
		assertTrue(job.getRuntime()>c12*1000);
		
		long r1 = job.getRuntime();
		this.sleep(100);		
		long r2 = job.getRuntime();
		assertTrue(r2>r1);
	}
	
	public void assertJobRunning(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobRunning(job);
		assertCountersAndDurationsJobRunning(job, maxCounter);
	}
	
	public void assertJobWaiting(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobWaiting(job);
		assertCountersAndDurationsJobWaiting(job, maxCounter);
	}
	
	public void assertJobSuccessful(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobSuccessful(job);
		assertCountersAndDurationsJobSuccessful(job, maxCounter);
	}
	
	public void assertJobAborted(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobAborted(job);
		assertCountersAndDurationsJobAborted(job, maxCounter);
	}
	
	public void assertJobError(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobError(job);
		assertCountersAndDurationsJobError(job, maxCounter);
	}
	
	public void assertJobTimeout(SampleJob job, long maxCounter, long timeout)
	{
		assertStatesAndFlagsJobTimeout(job);
		assertCountersAndDurationsJobTimeout(job, maxCounter, timeout);
	}
	
	public void assertJobPaused(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobPaused(job);
		assertCountersAndDurationsJobPaused(job, maxCounter);
	}
	
	public void assertJobSuccessfulAfterPause(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobSuccessful(job);
		assertCountersAndDurationsJobSuccessfulAfterPause(job, maxCounter);
	}
	
	public void assertJobRunningAfterPause(SampleJob job, long maxCounter)
	{
		assertStatesAndFlagsJobRunning(job);
		assertCountersAndDurationsJobRunningAfterPause(job, maxCounter);
	}
	
	public void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			new RuntimeException("pause interrupted");
		}
	}
	
	protected void logStart(MyLogger log, String msg)
	{
		log.info("######################################################################");
		log.info(msg);
	}

	protected void logEnd(MyLogger log, String msg, SampleJob... jobs)
	{
		for(SampleJob j: jobs)
		{
			log.info(j.jobStats());
		}
		log.info(msg);
		log.info("");
		log.info("");
	}

}
