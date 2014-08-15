package com.zwb.scheduler.dispatcher.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zwb.scheduler.dispatcher.api.IDispatcherWorkerJob;
import com.zwb.scheduler.impl.SchedulerJobBase;

public abstract class DispatcherWorkerJobBase<I, O> extends SchedulerJobBase<O> implements IDispatcherWorkerJob<I, O>
{
	private I inputData;
	private List<O> outputData = new ArrayList<O>();

	public IDispatcherWorkerJob<I, O> setInputData(I inputData)
	{
		this.inputData = inputData;
		return this;
	}

	public I getInputData()
	{
		return this.inputData;
	}

	@Override
	public IDispatcherWorkerJob<I, O> addOutputData(O... outputData)
	{
		synchronized (this.outputData)
		{
			for (O o : outputData)
			{
				this.outputData.add(o);
			}
		}
		return this;
	}

	@Override
	public IDispatcherWorkerJob<I, O> addOutputData(List<O> outputData)
	{
		synchronized (this.outputData)
		{
			this.outputData.addAll(outputData);
		}
		return this;
	}

	@Override
	public List<O> getOutputData()
	{
		return this.outputData;
	}

}
