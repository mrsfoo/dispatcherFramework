package com.zwb.scheduler.dispatcher.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zwb.scheduler.dispatcher.api.IDispatcherWorkerJob;
import com.zwb.scheduler.dispatcher.junit.tests.DispatcherTests;
import com.zwb.scheduler.impl.SchedulerJobBase;
import com.zwb.scheduler.util.MyLogger;

public class DispatcherJob<I, O> extends SchedulerJobBase<Void>
{
	private List<IDispatcherWorkerJob<I, O>> runningJobs = new ArrayList<IDispatcherWorkerJob<I, O>>();
	private Dispatcher<I, O> dispatcher;
	private static MyLogger log = new MyLogger(DispatcherJob.class);

	public DispatcherJob(Dispatcher<I, O> dispatcher)
	{
		this.dispatcher = dispatcher;
	}

	@Override
	public void preProcess()
	{
	}

	@Override
	public void postProcess()
	{
		this.cleanUpJobs();
	}

	@Override
	public void process()
	{
		while (true)
		{
			checkAllInterrupts();
			log.debug("working input and output queues...");
			log.debug("# inputQueueSize            :", this.dispatcher.getInputQueueSize());
			log.debug("# running jobs              :", this.runningJobs.size());
			log.debug("# outputQueueSize           :", this.dispatcher.getOutputQueue().size());

			/** create new worker jobs if input data present */
			while (this.dispatcher.getInputQueueSize() > 0)
			{
				checkAllInterrupts();
				I data = this.dispatcher.detachInputDataElement();
				log.debug("working data element in input queue: ", data);
				IDispatcherWorkerJob<I, O> job = this.dispatcher.createNewWorkerJob();
				job.setInputData(data);
				this.runningJobs.add(job);
				this.getScheduler().executeAsync(job);
			}

			/** handle finished worker jobs and write back output data */
			synchronized (this.runningJobs)
			{
				List<IDispatcherWorkerJob<I, O>> toDelete = new ArrayList<IDispatcherWorkerJob<I, O>>();
				for (IDispatcherWorkerJob<I, O> job : this.runningJobs)
				{
					checkAllInterrupts();
					switch (job.getState())
					{
					case ABORTED:
					case ERROR:
					case TIMEOUT:
					case SUCCESSFUL:
						log.debug("job in running queue is in state ", job.getState(), " -> writeBackOutputData");
						this.writeBackOutputData(job);
						toDelete.add(job);
						break;
					default:
						// DO NOTHING
						break;
					}

				}
				for (IDispatcherWorkerJob<I, O> job : toDelete)
				{
					this.runningJobs.remove(job);
				}
			}
			checkAllInterrupts();
			log.debug("going to sleep...");
			this.sleep(this.sleepCycle);
		}
	}

	@Override
	public void onError()
	{
		this.cleanUpJobs();
	}

	@Override
	public void onTimeout()
	{
		this.cleanUpJobs();
	}

	@Override
	public void onAbort()
	{
		this.cleanUpJobs();
	}

	private void cleanUpJobs()
	{
		log.debug("cleaning up all running jobs");
		synchronized (this.runningJobs)
		{
			for (IDispatcherWorkerJob<I, O> job : this.runningJobs)
			{
				switch (job.getState())
				{
				// job is finished; whatever output data is there will be wrote
				// to output queue
				case ABORTED:
				case ERROR:
				case TIMEOUT:
				case SUCCESSFUL:
					log.debug("job in running queue is in state ", job.getState(), " -> writeBackOutputData");
					this.writeBackOutputData(job);
					break;

				// job has not been started yet
				case WAITING:
					log.debug("job in running queue is in state ", job.getState(), " -> abort!");
					job.abortAsync();
				case INITALIZED:
					log.debug("job in running queue is in state ", job.getState(), " -> reset!");
					this.resetInputData(job);
					break;

				// job is running
				case PAUSED:
					job.resumeAsync();
				case RUNNING:
					log.debug("job in running queue is in state ", job.getState(), " -> abort and reset input data!");
					job.abortAsync();
					this.resetInputData(job);
					break;

				// bla
				default:
					// DO NOTHING
					break;
				}
			}
			this.runningJobs.clear();
		}
	}

	private void writeBackOutputData(IDispatcherWorkerJob<I, O> job)
	{
		log.debug("writing back output data: ", job.getOutputData());
		synchronized (job.getOutputData())
		{
			if (job.getOutputData() != null)
			{
				this.dispatcher.addToOutputQueue(job.getOutputData());
			}
		}
	}

	private void resetInputData(IDispatcherWorkerJob<I, O> job)
	{
		log.debug("resetting input data: ", job.getInputData());
		synchronized (job.getInputData())
		{
			if (job.getInputData() != null)
			{
				this.dispatcher.enqueue(job.getInputData());
			}
		}
	}

	public List<I> getWorkingSnapshot()
	{
		synchronized (this.runningJobs)
		{
			List<I> list = new ArrayList<I>();
			for (IDispatcherWorkerJob<I, O> j : this.runningJobs)
			{
				list.add(j.getInputData());
			}
			return list;
		}
	}

}
