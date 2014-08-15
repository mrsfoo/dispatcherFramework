package com.zwb.scheduler.junit.tests;

import java.util.ArrayList;
import java.util.List;

import com.zwb.scheduler.junit.BasicSchedulerTestCase;
import com.zwb.scheduler.junit.util.LoadMonitor;
import com.zwb.scheduler.junit.util.SampleJob;
import com.zwb.scheduler.util.MyLogger;

public class StressTests extends BasicSchedulerTestCase
{
	private static MyLogger log = new MyLogger(StressTests.class);

	public void testSchedulerStressTest01()
	{
		logStart(log, "starting testSchedulerStressTest01");
		
		int noOfJobs = 300;
		int noOfThreads = 100;
		long maxCount = 5;

		scheduler.setMaxThread(noOfThreads);
		scheduler.setSleepCycle(50);
		
		List<SampleJob> jobs = new ArrayList<SampleJob>();
		for(int i=0; i<noOfJobs; i++)
		{
			SampleJob j = new SampleJob(maxCount);
			j.setSleepCycle(300);
			j.setName("SampleJob"+i);
			j.setPostProcessingDelay((long)Math.random()*50);
			j.setPreProcessingDelay((long)Math.random()*50);
			jobs.add(j);
		}
		for(SampleJob j: jobs)
		{
			this.scheduler.executeAsync(j);			
		}
		
		this.sleep(1000);
		scheduler.waitUntilNoJobsWaitingOrRunning();
		
		for(SampleJob j: jobs)
		{
			assertStatesAndFlagsJobSuccessful(j);
			assertEquals(maxCount, j.getResult().longValue());
		}
		
		logEnd(log, "finished testSchedulerStressTest01");
	}

//	public void testSchedulerStressTest02()
//	{
//		logStart(log, "starting testSchedulerStressTest02");
//		
//		int noOfJobs = 3000;
//		int noOfThreads = 1000;
//		long maxCount = 2;
//
//		scheduler.setMaxThread(noOfThreads);
//		scheduler.setSleepCycle(50);
//		
//		List<SampleJob> jobs = new ArrayList<SampleJob>();
//		for(int i=0; i<noOfJobs; i++)
//		{
//			SampleJob j = new SampleJob(maxCount);
//			j.setSleepCycle(300);
//			j.setName("SampleJob"+i);
//			j.setPostProcessingDelay((long)Math.random()*50);
//			j.setPreProcessingDelay((long)Math.random()*50);
//			jobs.add(j);
//		}
//		for(SampleJob j: jobs)
//		{
//			this.scheduler.executeAsync(j);			
//		}
//		
//		this.sleep(1000);
//		scheduler.waitUntilNoJobsWaitingOrRunning();
//		
//		for(SampleJob j: jobs)
//		{
//			assertStatesAndFlagsJobSuccessful(j);
//			assertEquals(maxCount, j.getResult().longValue());
//		}
//		
//		logEnd(log, "finished testSchedulerStressTest02");
//	}

	public void testSchedulerLoadBalacing()
	{
		logStart(log, "starting testSchedulerLoadBalacingTest");
		
		LoadMonitor monitor = new LoadMonitor(300);
		this.scheduler.executeAsync(monitor);
		
		int noOfJobs = 300;
		int noOfThreads = 100;
		long maxCount = 5;

		scheduler.setMaxThread(noOfThreads);
		scheduler.setSleepCycle(50);
		
		List<SampleJob> jobs = new ArrayList<SampleJob>();
		for(int i=0; i<noOfJobs; i++)
		{
			SampleJob j = new SampleJob(maxCount);
			j.setSleepCycle(300);
			j.setName("SampleJob"+i);
			j.setPostProcessingDelay((long)Math.random()*50);
			j.setPreProcessingDelay((long)Math.random()*50);
			jobs.add(j);
		}
		for(SampleJob j: jobs)
		{
			this.scheduler.executeAsync(j);
		}
		
		scheduler.waitUntilNoJobsWaiting();
		scheduler.waitUntilRunningJobCountIsBelow(2);
		monitor.abortSync();
		scheduler.waitUntilNoJobsWaitingOrRunning();
		
		for(SampleJob j: jobs)
		{
			assertStatesAndFlagsJobSuccessful(j);
			assertEquals(maxCount, j.getResult().longValue());
		}
		
		log.info("\n"+monitor.getLoadTable());
		assertEqualsFuzzy(noOfThreads, monitor.getMeanRunningWhileAtLeastOneWaiting(), 0.1);
		
		logEnd(log, "finished testSchedulerLoadBalacingTest");
	}

	public void testSchedulingOverhead()
	{
		logStart(log, "starting testSchedulingOverhead");
		
		int noOfJobs = 50;
		int noOfThreads = 1;
		long maxCount = 2;
		long preProcessingDelayMs = 100;
		long postProcessingDelayMs = 100;
		
		scheduler.setMaxThread(noOfThreads);
		scheduler.setSleepCycle(50);
		
		List<SampleJob> jobs = new ArrayList<SampleJob>();
		for(int i=0; i<noOfJobs; i++)
		{
			SampleJob j = new SampleJob(maxCount);
			j.setSleepCycle(50);
			j.setName("SampleJob"+i);
			j.setPostProcessingDelay(postProcessingDelayMs);
			j.setPreProcessingDelay(preProcessingDelayMs);
			jobs.add(j);
		}

		long startTS = System.currentTimeMillis();
		for(SampleJob j: jobs)
		{
			this.scheduler.executeAsync(j);
		}
		
		scheduler.waitUntilNoJobsWaitingOrRunning();
		double runtime = System.currentTimeMillis()-startTS;
		
		double payloadTimePerJobMs = preProcessingDelayMs + maxCount*1000 + postProcessingDelayMs;
		double totalPayloadTime = payloadTimePerJobMs*noOfJobs;
		double totalRuntimePerJob = runtime/noOfJobs;
		double overheadTime = runtime-totalPayloadTime;
		double overheadTimePerJob = overheadTime/noOfJobs;
		
		for(SampleJob j: jobs)
		{
			assertStatesAndFlagsJobSuccessful(j);
			assertEquals(maxCount, j.getResult().longValue());
		}
		
		log.info("++++++++++++++++++++++++++++++++++++++");
		log.info("TOTAL RUNTIME              [s] : "+runtime/1000);
		log.info("TOTAL PAYLOAD TIME         [s] : "+totalPayloadTime/1000);
		log.info("TOTAL OVERHEAD TIME        [s] : "+overheadTime/1000);
		log.info("TOTAL RUNTIME PER JOB      [s] : "+totalRuntimePerJob/1000);
		log.info("TOTAL PAYLOAD TIME PER JOB [s] : "+payloadTimePerJobMs/1000);
		log.info("OVERHEAD TIME PER JOB      [s] : "+overheadTimePerJob/1000);
		log.info("--------------------------------------");
		log.info("OVERHEAD TIME              [%] : "+overheadTime/runtime*100);
		log.info("++++++++++++++++++++++++++++++++++++++");
		
		logEnd(log, "finished testSchedulingOverhead");
	}

}
