package com.zwb.scheduler.dispatcher.api;

import com.zwb.scheduler.dispatcher.impl.Dispatcher;

public class DispatcherFactory
{
	public static <I, O> IDispatcher<I, O> createDispatcher(Class<I> inputDataType, Class<O> outputDataType, Class<? extends IDispatcherWorkerJob<I, O>> jobClass)
	{
		Dispatcher<I, O> d = new Dispatcher<I, O>(inputDataType, outputDataType, jobClass);
		return d;
	}

}
