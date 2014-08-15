package com.zwb.scheduler.dispatcher.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.zwb.scheduler.dispatcher.api.DispatcherFactory;
import com.zwb.scheduler.dispatcher.api.IDispatcher;
import com.zwb.scheduler.dispatcher.junit.util.DispatcherMonitor;
import com.zwb.scheduler.dispatcher.junit.util.ListOfInt;
import com.zwb.scheduler.dispatcher.junit.util.SampleDispatcherWorkerJob01;
import com.zwb.scheduler.dispatcher.junit.util.SampleDispatcherWorkerJob02;
import com.zwb.scheduler.junit.util.SampleJob;
import com.zwb.scheduler.util.MyLogger;

public class BasicDispatcherTestCase extends TestCase
{
	private static MyLogger log = new MyLogger(BasicDispatcherTestCase.class);
	
	protected IDispatcher<ListOfInt, String> dispatcher01;
	protected IDispatcher<String, String> dispatcher02;
	protected List<ListOfInt> inputData01_1 = new ArrayList<ListOfInt>();
	protected List<ListOfInt> inputData01_2 = new ArrayList<ListOfInt>();
	protected List<ListOfInt> inputData01_3 = new ArrayList<ListOfInt>();

	protected DispatcherMonitor<ListOfInt, String> monitor01;
	protected DispatcherMonitor<String, String> monitor02;
	
	private static final long MONITOR_TIME_GRANULARITY = 100;
	
	@Override
	public void setUp()
	{
		log.info("");
		log.info("starting test setUp");
		
//		this.dispatcher01 = DispatcherFactory.createDispatcher(new ArrayList<Integer>().getClass(), String.class, SampleDispatcherWorkerJob01.class);
		this.dispatcher01 = DispatcherFactory.createDispatcher(ListOfInt.class, String.class, SampleDispatcherWorkerJob01.class);
		this.dispatcher02 = DispatcherFactory.createDispatcher(String.class, String.class, SampleDispatcherWorkerJob02.class);
		
		this.dispatcher01.setSchedulerMaxThread(5);
		this.dispatcher02.setSchedulerMaxThread(5);
		
		SampleDispatcherWorkerJob01.setProcessionDelay(100);
		SampleDispatcherWorkerJob02.setProcessionDelay(100);
		SampleDispatcherWorkerJob01.setProcessionDeviation(0.5);
		SampleDispatcherWorkerJob02.setProcessionDeviation(0.5);
		
		this.monitor01 = new DispatcherMonitor<ListOfInt, String>(this.dispatcher01, MONITOR_TIME_GRANULARITY);
		this.monitor02 = new DispatcherMonitor<String, String>(this.dispatcher02, MONITOR_TIME_GRANULARITY);
		
		this.buildInputData();
		
		log.info("finished test setUp");
		log.info("");
	}
	
	@Override
	public void tearDown()
	{
		log.info("");
		log.info("starting test tearDown");
		this.dispatcher01.stop();
		this.dispatcher02.stop();
		log.info("finished test tearDown");
		log.info("");
	}
	
	public void assertEqualsFuzzy(double expected, double actual, double deviation)
	{
		assertEquals(expected, actual, (expected*deviation));
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

	protected void logEnd(MyLogger log, String msg)
	{
		log.info(msg);
		log.info("");
		log.info("");
	}
	
	private void buildInputData()
	{
		addInputData01(inputData01_1, 1,2,3,4,5);
		addInputData01(inputData01_1, 2,3,4,5);
		addInputData01(inputData01_1, 3,4,5);
		addInputData01(inputData01_1, 4,5);
		addInputData01(inputData01_1, 5);
		
		addInputData01(inputData01_2, 10,11,12);
		addInputData01(inputData01_2, 13,14,15);
		addInputData01(inputData01_2, 16,17,18);
		addInputData01(inputData01_2, 19,20,21);
		addInputData01(inputData01_2, 22,23,24);
		addInputData01(inputData01_2, 25,26,27);

		addInputData01(inputData01_3, 100,200,300);
		addInputData01(inputData01_3, 200,300,400);
		addInputData01(inputData01_3, 300,400,500);
}
	
	private void addInputData01(List<ListOfInt> input, int... numbers)
	{
		ListOfInt list = new ListOfInt();
		for(int i: numbers)
		{
			list.list.add(i);
		}
		input.add(list);
	}
}
