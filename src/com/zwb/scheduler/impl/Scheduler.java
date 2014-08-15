package com.zwb.scheduler.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.zwb.scheduler.api.Defaults;
import com.zwb.scheduler.api.IScheduler;
import com.zwb.scheduler.api.ISchedulerJob;
import com.zwb.scheduler.api.ISchedulerJobBase;
import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.exception.SchedulerJobRuntimeException;
import com.zwb.scheduler.util.MyLogger;
import com.zwb.scheduler.util.ThreadUtils;

public class Scheduler implements IScheduler, Runnable
{
	private static MyLogger log = new MyLogger(Scheduler.class);

	private List<SchedulerJobBase<?>> jobWaitingQueue = new ArrayList<SchedulerJobBase<?>>();
	private List<SchedulerJobBase<?>> jobsRunning = new ArrayList<SchedulerJobBase<?>>();

	private int maxThreads = Defaults.SCHEDULER_MAX_THREADS;
	private long sleepCycle = Defaults.SCHEDULER_SLEEP_CYCLE;
	private boolean isPaused = false;
	private boolean isStopped = true;
	private long pauseTimeout = -1;
	private long pauseStartedTimestamp = -1;

	private String description = "";
	private String name = "";

	ExecutorService threadPool = ThreadUtils.createThreadPool(false);

	private void runJob(SchedulerJobBase<?> job)
	{
		if(!job.isWaiting())
		{
			return;
		}
		job.setJobState(SchedulerJobState.RUNNING);
		log.debug("pre-processing job: ", job.getName());
		job.setTimestampStartOfPreProcessing(System.currentTimeMillis());
		job.preProcess();
		job.setTimestampStopOfPreProcessing(System.currentTimeMillis());
		ThreadUtils.runInThread("Job", job.getName(), this.threadPool, job);
	}

	@Override
	public void start()
	{
		log.info("starting scheduler ", this.getName(), "-", this.hashCode(), " [", this.getDescription(), "]");
		log.debug("starting dispatcher main thread");
		this.isStopped = false;
		ThreadUtils.runInThread("Sched", this.getName(), this.threadPool, this);
	}

	@Override
	public void pause()
	{
		log.debug("pause scheduler");
		this.isPaused = true;
		this.pauseTimeout = -1;
		this.pauseStartedTimestamp = -1;
	}

	@Override
	public void pause(long timeoutMillis)
	{
		log.debug("pause scheduler with timeout ", timeoutMillis, "ms");
		this.isPaused = true;
		this.pauseTimeout = timeoutMillis;
		this.pauseStartedTimestamp = System.currentTimeMillis();
	}

	@Override
	public void resume()
	{
		log.debug("resume scheduler");
		this.isPaused = false;
		this.pauseTimeout = -1;
		this.pauseStartedTimestamp = -1;
	}

	@Override
	public void pauseAll()
	{
		log.debug("pauseAll");
		this.pause();

		synchronized (this.jobWaitingQueue)
		{
			log.trace("entered synchronized(jobWaitingQueue)");
			for(ISchedulerJob<?> j: this.jobWaitingQueue)
			{
				j.pauseAsync();
			}
		}
		
		log.trace("waiting for synchronized(jobsRunning)");
		synchronized (this.jobsRunning)
		{
			log.trace("entered synchronized(jobsRunning)");
			for(ISchedulerJob<?> j: this.jobsRunning)
			{
				j.pauseAsync();
			}
		}
	}

	@Override
	public void pauseAll(long timeoutMillis)
	{
		log.debug("pauseAll with timeout ", timeoutMillis, "ms");
		this.pause(timeoutMillis);

		synchronized (this.jobWaitingQueue)
		{
			log.trace("entered synchronized(jobWaitingQueue)");
			for(ISchedulerJob<?> j: this.jobWaitingQueue)
			{
				j.pauseAsync(timeoutMillis);
			}
		}
		
		log.trace("waiting for synchronized(jobsRunning)");
		synchronized (this.jobsRunning)
		{
			log.trace("entered synchronized(jobsRunning)");
			for(ISchedulerJob<?> j: this.jobsRunning)
			{
				j.pauseAsync(timeoutMillis);
			}
		}
	}

	@Override
	public void resumeAll()
	{
		log.debug("resumeAll");
		synchronized (this.jobWaitingQueue)
		{
			log.trace("entered synchronized(jobWaitingQueue)");
			for(ISchedulerJob<?> j: this.jobWaitingQueue)
			{
				j.resumeAsync();
			}
		}
		
		log.trace("waiting for synchronized(jobsRunning)");
		synchronized (this.jobsRunning)
		{
			log.trace("entered synchronized(jobsRunning)");
			for(ISchedulerJob<?> j: this.jobsRunning)
			{
				j.resumeAsync();
			}
		}
		
		this.resume();
	}

	@Override
	public final <T> T executeSync(ISchedulerJob<T> job)
	{
		if (!job.getState().equals(SchedulerJobState.INITALIZED))
		{
			return null;
		}
		log.debug("executeSync job: ", job.getName());
		((SchedulerJobBase<?>) job).setScheduler(this);
		this.startJob(job);
		job.waitForFinishing();
		return job.getResult();
	}

	@Override
	public final void executeAsync(ISchedulerJob<?> job)
	{
		if (!job.getState().equals(SchedulerJobState.INITALIZED))
		{
			return;
		}
		log.debug("executeAsync job: ", job.getName());
		((SchedulerJobBase<?>) job).setScheduler(this);
		this.startJob(job);
	}

	public void startJob(ISchedulerJobBase<?> job)
	{
		log.debug("starting job: ", job.getName());
		log.trace("waiting for synchronized(jobWaitingQueue)");
		synchronized (this.jobWaitingQueue)
		{
			log.trace("entered synchronized(jobWaitingQueue)");
			this.jobWaitingQueue.add((SchedulerJobBase<?>) job);
			((SchedulerJobBase<?>) job).setJobState(SchedulerJobState.WAITING);
		}
	}

	@Override
	public IScheduler setMaxThread(int max)
	{
		this.maxThreads = max;
		return this;
	}

	@Override
	public void run()
	{
		ThreadUtils.setCurrentThreadParams(this, "Sched", this.getName());
		while (!this.isStopped)
		{
			log.debug("starting dispatcher cycle...");
			log.debug("# jobs waiting            :", this.jobWaitingQueue.size());
			log.debug("# jobs running            :", this.jobsRunning.size());

			List<SchedulerJobBase<?>> jobsEnded = new ArrayList<SchedulerJobBase<?>>();
			log.trace("waiting for synchronized(jobsRunning, jobWaitingQueue)");
			int jobsEndedCnt = 0;
			int jobsStartedCnt = 0;

			/** clean up finished jobs */
			synchronized (this.jobsRunning)
			{
				synchronized (this.jobWaitingQueue)
				{
					log.trace("entered synchronized(jobsRunning, jobWaitingQueue)");
					for (SchedulerJobBase<?> j : this.jobsRunning)
					{
						boolean jobDone = false;
						if (j.getJobStateTransitional() != null)
						{
							jobDone = true;
							switch (j.getJobStateTransitional())
							{
							case ABORTED:
								log.debug("realized job transition: ", j.getName(), ": ", j.getState(), "->", j.getJobStateTransitional());
								j.onAbort();
								j.setJobState(SchedulerJobState.ABORTED);
								j.setJobStateTransitional(null);
								break;
							case ERROR:
								log.debug("realized job transition: ", j.getName(), ": ", j.getState(), "->", j.getJobStateTransitional());
								j.onError();
								j.setJobState(SchedulerJobState.ERROR);
								j.setJobStateTransitional(null);
								break;
							case TIMEOUT:
								log.debug("realized job transition: ", j.getName(), ": ", j.getState(), "->", j.getJobStateTransitional());
								j.onTimeout();
								j.setJobState(SchedulerJobState.TIMEOUT);
								j.setJobStateTransitional(null);
								break;
							case SUCCESSFUL:
								log.debug("realized job transition: ", j.getName(), ": ", j.getState(), "->", j.getJobStateTransitional());
								j.setTimestampStartOfPostProcessing(System.currentTimeMillis());
								log.debug("post-processing job: ", j.getName());
								j.postProcess();
								j.setTimestampStopOfPostProcessing(System.currentTimeMillis());
								j.setJobState(SchedulerJobState.SUCCESSFUL);
								j.setJobStateTransitional(null);
								break;
							default:
								jobDone = false;
								break;
							}
						}
						if (jobDone)
						{
							jobsEnded.add(j);
						}
					}

					for (SchedulerJobBase<?> j : jobsEnded)
					{
						this.jobsRunning.remove(j);
					}
					jobsEndedCnt = jobsEnded.size();
					jobsEnded.clear();

					/** check pause timeout */
					if (this.isPaused && (this.pauseTimeout >= 0))
					{
						if ((System.currentTimeMillis() - this.pauseStartedTimestamp) > this.pauseTimeout)
						{
							log.debug("pause timeout");
							this.resume();
						}
					}
					/** start new jobs */
					if (!this.isPaused)
					{
						while ((!this.jobWaitingQueue.isEmpty()) && (this.jobsRunning.size() < this.maxThreads))
						{
							SchedulerJobBase<?> job = this.jobWaitingQueue.remove(0);
							log.debug(this.jobsRunning.size(), " running jobs; starting new job: ", job.getName());
							this.jobsRunning.add(job);
							this.runJob(job);
							jobsStartedCnt++;
						}
					}
				}
			}
			log.debug("ending dispatcher cycle...");
			log.debug("# jobs waiting            :", this.jobWaitingQueue.size());
			log.debug("# jobs running            :", this.jobsRunning.size());
			log.debug("# jobs started this cycle :", jobsStartedCnt);
			log.debug("# jobs ended this cycle   :", jobsEndedCnt);
			log.debug("...going to sleep");
			this.sleep(this.sleepCycle);
		}
		shutdown();
	}

	private void shutdown()
	{
		log.info("exiting scheduler");
		log.trace("waiting for synchronized(jobWaitingQueue)");
		synchronized (this.jobWaitingQueue)
		{
			log.trace("entered synchronized(jobWaitingQueue)");
			for(ISchedulerJob<?> j: this.jobWaitingQueue)
			{
				j.abortAsync();
			}
			this.jobWaitingQueue.clear();
		}
		
		log.trace("waiting for synchronized(jobsRunning)");
		synchronized (this.jobsRunning)
		{
			log.trace("entered synchronized(jobsRunning)");
			for(ISchedulerJob<?> j: this.jobsRunning)
			{
				j.abortAsync();
			}
			this.jobsRunning.clear();
		}
		
		this.threadPool.shutdown();
	}

	@Override
	public IScheduler setSleepCycle(long sleepCycleMillis)
	{
		this.sleepCycle = sleepCycleMillis;
		return this;
	}

	public IScheduler setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public IScheduler setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void waitUntilWaitingJobCountIsBelow(int jobCount)
	{
		this.waitUntilWaitingJobCountIsBelow(jobCount, -42);
	}

	@Override
	public void waitUntilRunningJobCountIsBelow(int jobCount)
	{
		this.waitUntilRunningJobCountIsBelow(jobCount, -42);
	}

	@Override
	public boolean waitUntilWaitingJobCountIsBelow(int jobCount, long timeoutMillis)
	{
		long timestampWaitingStarted = System.currentTimeMillis();
		log.debug("wait scheduler until waiting job queue size is below ", jobCount, "; currently it is ", this.jobWaitingQueue.size(), "; waiting timeout=", timeoutMillis, "ms");
		while (true)
		{
			if ((timeoutMillis >= 0) && ((System.currentTimeMillis() - timestampWaitingStarted) > timeoutMillis))
			{
				log.debug("wait scheduler timeout");
				return true;
			}
			log.trace("waiting for synchronized(jobWaitingQueue)");
			synchronized (this.jobWaitingQueue)
			{
				log.trace("entered synchronized(jobWaitingQueue)");
				if (this.jobWaitingQueue.size() < jobCount)
				{
					return false;
				}
			}
			this.sleep(this.sleepCycle);
		}
	}

	@Override
	public boolean waitUntilRunningJobCountIsBelow(int jobCount, long timeoutMillis)
	{
		long timestampWaitingStarted = System.currentTimeMillis();
		log.debug("wait scheduler until running job count is below ", jobCount, "; currently it is ", this.jobsRunning.size(), "; waiting timeout=", timeoutMillis, "ms");
		while (true)
		{
			if ((timeoutMillis >= 0) && ((System.currentTimeMillis() - timestampWaitingStarted) > timeoutMillis))
			{
				log.debug("wait scheduler timeout");
				return true;
			}
			log.trace("waiting for synchronized(jobsRunning)");
			synchronized (this.jobsRunning)
			{
				log.trace("entered synchronized(jobsRunning)");
				if (this.jobsRunning.size() < jobCount)
				{
					return false;
				}
			}
			this.sleep(this.sleepCycle);
		}
	}

	@Override
	public void waitUntilNoJobsWaiting()
	{
		waitUntilWaitingJobCountIsBelow(1);
	}

	@Override
	public void waitUntilNoJobsRunning()
	{
		waitUntilRunningJobCountIsBelow(1);
	}

	public void waitUntilNoJobsWaitingOrRunning()
	{
		waitUntilNoJobsWaitingOrRunning(-42);
	}

	@Override
	public boolean waitUntilNoJobsWaitingOrRunning(long timeoutMillis)
	{
		long timestampWaitingStarted = System.currentTimeMillis();
		log.debug("wait scheduler until no jobs running or in waiting queue; currently there are ", this.jobWaitingQueue.size(), " in job waiting queue and ", this.jobsRunning.size(), " jobs running", "; waiting timeout=", timeoutMillis, "ms");
		while (true)
		{
			if ((timeoutMillis >= 0) && ((System.currentTimeMillis() - timestampWaitingStarted) > timeoutMillis))
			{
				log.debug("wait scheduler timeout");
				return true;
			}
			log.trace("waiting for synchronized(jobsRunning, jobWaitingQueue)");
			synchronized (this.jobWaitingQueue)
			{
				synchronized (this.jobsRunning)
				{
					log.trace("entered synchronized(jobsRunning, jobWaitingQueue)");
					if (this.jobsRunning.isEmpty() && this.jobWaitingQueue.isEmpty())
					{
						return false;
					}
				}
			}
			this.sleep(this.sleepCycle);
		}
	}

	protected final void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			new SchedulerJobRuntimeException("sleep interrupted");
		}
	}

	public int getNumberObJobsRunning()
	{
		return this.jobsRunning.size();
	}

	public int getNumberObJobsWaiting()
	{
		return this.jobWaitingQueue.size();
	}

	@Override
	public boolean waitUntilNoJobsWaiting(long timeoutMillis)
	{
		return waitUntilWaitingJobCountIsBelow(1, timeoutMillis);
	}

	@Override
	public boolean waitUntilNoJobsRunning(long timeoutMillis)
	{
		return waitUntilRunningJobCountIsBelow(1, timeoutMillis);
	}

	@Override
	public void stop()
	{
		this.isStopped = true;
	}

	@Override
	public boolean isRunning()
	{
		return !isStopped;
	}
}
