package com.zwb.scheduler.dispatcher.junit.util;

import java.util.ArrayList;
import java.util.List;

public class DispatcherMonitorData
{
	long timestamp;
	List<String> inputQueue = new ArrayList<String>();
	List<String> outputQueue = new ArrayList<String>();
	List<String> workingQueue = new ArrayList<String>();
	
//	public DispatcherMonitorData(List<String> inputQueue, List<String> outputQueue, List<String> workingQueue)
//	{
//		this.inputQueue = inputQueue;
//		this.outputQueue = outputQueue;
//		this.workingQueue = workingQueue;
//	}
//	
	public <I,O,W> DispatcherMonitorData(long timestamp, List<I> inputQueue, List<O> outputQueue, List<W> workingQueue)
	{
		this.timestamp = timestamp;
		for(I i: inputQueue)
		{
			this.inputQueue.add(i.toString());
		}
		for(O o: outputQueue)
		{
			this.outputQueue.add(o.toString());
		}
		for(W w: workingQueue)
		{
			this.workingQueue.add(w.toString());
		}
	}
	
}
