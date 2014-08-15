package com.zwb.scheduler.dispatcher.junit.tests;

import java.util.ArrayList;
import java.util.List;

import com.zwb.scheduler.dispatcher.junit.BasicDispatcherTestCase;
import com.zwb.scheduler.util.MyLogger;

public class DispatcherTests extends BasicDispatcherTestCase
{
	private static MyLogger log = new MyLogger(DispatcherTests.class);

	public void testSimpleDispatcher()
	{
		logStart(log, "testSimpleDispatcher");
		this.monitor01.startSync();
		this.dispatcher01.start();
		this.dispatcher01.enqueue(this.inputData01_1);
		this.sleep(500);
		this.dispatcher01.enqueue(this.inputData01_2);
		this.sleep(500);
		this.dispatcher01.enqueue(this.inputData01_3);
		this.sleep(10000);
		
		this.dispatcher01.stop();
		this.monitor01.stopSync();
		this.monitor01.printFormatted();
		logEnd(log, "finished testSimpleDispatcher");
	}
	
	public void testChainDispatchers()
	{
		logStart(log, "testChainDispatchers");
		this.monitor01.startSync();
		this.monitor02.startSync();
		this.dispatcher01.start();
		this.dispatcher02.start();
		
		this.dispatcher01.enqueue(this.inputData01_1);
		this.sleep(500);

		this.dispatcher02.chainAfterDispatcher(dispatcher01);
		
		this.dispatcher01.enqueue(this.inputData01_2);
		this.sleep(500);
		this.dispatcher01.enqueue(this.inputData01_3);
		this.sleep(10000);
		
		this.dispatcher01.stop();
		this.dispatcher02.stop();
		this.monitor01.stopSync();
		this.monitor02.stopSync();
		this.monitor01.printFormatted();
		this.monitor02.printFormatted();
		logEnd(log, "finished testChainDispatchers");
	}

}
