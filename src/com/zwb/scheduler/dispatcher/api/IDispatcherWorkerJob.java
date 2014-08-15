package com.zwb.scheduler.dispatcher.api;

import java.util.Collection;
import java.util.List;

import com.zwb.scheduler.api.ISchedulerJob;

public interface IDispatcherWorkerJob<I, O> extends ISchedulerJob<O>
{
	public IDispatcherWorkerJob<I, O> setInputData(I inputData);
	public I getInputData();
	public IDispatcherWorkerJob<I, O> addOutputData(O... outputData);
	public IDispatcherWorkerJob<I, O> addOutputData(List<O> outputData);
	public List<O> getOutputData();
}
