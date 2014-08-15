package com.zwb.scheduler.impl;

import java.util.Arrays;

import com.zwb.scheduler.api.Defaults;
import com.zwb.scheduler.api.IScheduler;
import com.zwb.scheduler.api.ISchedulerJob;
import com.zwb.scheduler.api.ISchedulerJobBase;
import com.zwb.scheduler.api.SchedulerJobError;
import com.zwb.scheduler.api.SchedulerJobState;
import com.zwb.scheduler.exception.SchedulerJobAbortionException;
import com.zwb.scheduler.exception.SchedulerJobErrorException;
import com.zwb.scheduler.exception.SchedulerJobRuntimeException;
import com.zwb.scheduler.exception.SchedulerJobSuccessException;
import com.zwb.scheduler.exception.SchedulerJobTimoutException;
import com.zwb.scheduler.util.MyLogger;
import com.zwb.scheduler.util.ThreadUtils;

public abstract class SchedulerJobBase<T> implements ISchedulerJob<T>, Runnable
{
	private static MyLogger log = new MyLogger(SchedulerJobBase.class);

	private boolean abortCalled = false;
	private boolean pauseCalled = false;
	private SchedulerJobState jobState = SchedulerJobState.INITALIZED;
	private SchedulerJobState jobStateTransitional = null;
	private SchedulerJobError jobError = null;

	private Scheduler schedulder = null;

	protected long sleepCycle = Defaults.SCHEDULER_JOB_SLEEP_CYCLE;
	private long pauseTime = -1;

	private long jobTimeout = -1;
	private long timestampStartOfPreProcessing = -1;
	private long timestampStopOfPreProcessing = -1;
	private long timestampStartOfProcessing = -1;
	private long timestampStopOfProcessing = -1;
	private long timestampStartOfPostProcessing = -1;
	private long timestampStopOfPostProcessing = -1;

	private String jobDescription = "";
	private String jobName = "";

	private T jobResult;

	@Override
	public void run()
	{
		try
		{
			this.timestampStartOfProcessing = System.currentTimeMillis();
			ThreadUtils.setCurrentThreadParams(this, "Job", this.getName());
			this.process();
			log.debug("job run method ended; transition to state SUCCESSFUL");
			this.setJobStateTransitional(SchedulerJobState.SUCCESSFUL);
		}
		catch (SchedulerJobTimoutException e)
		{
			log.debug("interrupt with job transition to state TIMEOUT received");
			this.setJobStateTransitional(SchedulerJobState.TIMEOUT);
		}
		catch (SchedulerJobAbortionException e)
		{
			log.debug("interrupt with job transition to state ABORTED received");
			this.setJobStateTransitional(SchedulerJobState.ABORTED);
		}
		catch (SchedulerJobErrorException e)
		{
			log.debug("interrupt with job transition to state ERROR received");
			this.setJobStateTransitional(SchedulerJobState.ERROR);
		}
		catch (SchedulerJobSuccessException e)
		{
			log.debug("interrupt with job transition to state SUCCESSFUL received");
			this.setJobStateTransitional(SchedulerJobState.SUCCESSFUL);
		}
		this.timestampStopOfProcessing = System.currentTimeMillis();
	}

	@Override
	public final T getResult()
	{
		if (this.jobResult == null)
		{
			synchronized (this)
			{
				return this.jobResult;
			}
		}
		else
		{
			synchronized (this.jobResult)
			{
				return this.jobResult;
			}
		}
	}

	public void setResult(T result)
	{
		if (this.jobResult == null)
		{
			synchronized (this)
			{
				this.jobResult = result;
			}
		}
		else
		{
			synchronized (this.jobResult)
			{
				this.jobResult = result;
			}
		}
	}

	@Override
	public final void abortAsync()
	{
		log.debug("abortAsync job ", this.getName());
		this.abortCalled = true;
	}

	@Override
	public final void abortSync()
	{
		log.debug("abortSync job ", this.getName());
		this.abortAsync();
		this.waitForStates(SchedulerJobState.ABORTED);
	}

	@Override
	public final void pauseAsync()
	{
		log.debug("pauseAsync job ", this.getName());
		this.pauseCalled = true;
		this.pauseTime = -1;
	}

	@Override
	public final void pauseSync()
	{
		log.debug("pauseSync job ", this.getName());
		this.pauseAsync();
		this.waitForStates(SchedulerJobState.PAUSED);
	}

	@Override
	public final void pauseAsync(long pauseTimeMillis)
	{
		log.debug("pauseAsync job ", this.getName());
		this.pauseCalled = true;
		this.pauseTime = pauseTimeMillis;
	}

	@Override
	public final void pauseSync(long pauseTimeMillis)
	{
		log.debug("pauseSync job ", this.getName());
		this.pauseAsync(pauseTimeMillis);
		this.waitForStates(SchedulerJobState.PAUSED);
	}

	@Override
	public final void resumeAsync()
	{
		log.debug("resumeAsync job ", this.getName());
		this.pauseCalled = false;
	}

	@Override
	public final void resumeSync()
	{
		log.debug("resumeSync job ", this.getName());
		this.pauseCalled = false;
		this.waitForStatesOtherThan(SchedulerJobState.PAUSED);
	}

	public final void setJobState(SchedulerJobState state)
	{
		this.jobState = state;
	}

	public final void setJobStateTransitional(SchedulerJobState state)
	{
		this.jobStateTransitional = state;
	}

	protected final void setJobError(SchedulerJobError error)
	{
		this.jobError = error;
	}

	@Override
	public final SchedulerJobState getState()
	{
		return this.jobState;
	}

	public final SchedulerJobState getJobStateTransitional()
	{
		return this.jobStateTransitional;
	}

	@Override
	public final SchedulerJobError getError()
	{
		return this.jobError;
	}

	@Override
	public final long getRuntime()
	{
		if (this.isRunning() || this.isPaused())
		{
			return Math.max(0, System.currentTimeMillis() - this.timestampStartOfProcessing);
		}
		else
		{
			return Math.max(0, this.getProcessingDuration());
		}
	}

	@Override
	public final long getPreProcessingDuration()
	{
		return Math.max(-1, this.timestampStopOfPreProcessing - this.timestampStartOfPreProcessing);
	}

	public final SchedulerJobBase<T> setTimestampStartOfPreProcessing(long timestamp)
	{
		this.timestampStartOfPreProcessing = timestamp;
		return this;
	}

	public final SchedulerJobBase<T> setTimestampStopOfPreProcessing(long timestamp)
	{
		this.timestampStopOfPreProcessing = timestamp;
		return this;
	}

	public final SchedulerJobBase<T> setTimestampStartOfPostProcessing(long timestamp)
	{
		this.timestampStartOfPostProcessing = timestamp;
		return this;
	}

	public final SchedulerJobBase<T> setTimestampStopOfPostProcessing(long timestamp)
	{
		this.timestampStopOfPostProcessing = timestamp;
		return this;
	}

	@Override
	public final long getPostProcessingDuration()
	{
		return Math.max(-1, this.timestampStopOfPostProcessing - this.timestampStartOfPostProcessing);
	}

	@Override
	public final long getProcessingDuration()
	{
		return Math.max(-1, this.timestampStopOfProcessing - this.timestampStartOfProcessing);
	}

	@Override
	public final boolean isInitialized()
	{
		return this.getState().equals(SchedulerJobState.INITALIZED);
	}

	@Override
	public final boolean isWaiting()
	{
		return this.getState().equals(SchedulerJobState.WAITING);
	}

	@Override
	public final boolean isRunning()
	{
		return this.getState().equals(SchedulerJobState.RUNNING);
	}

	@Override
	public final boolean isSuccessful()
	{
		return this.getState().equals(SchedulerJobState.SUCCESSFUL);
	}

	@Override
	public final boolean isAborted()
	{
		return this.getState().equals(SchedulerJobState.ABORTED);
	}

	@Override
	public final boolean isError()
	{
		return this.getState().equals(SchedulerJobState.ERROR);
	}

	@Override
	public final boolean isTimeout()
	{
		return this.getState().equals(SchedulerJobState.TIMEOUT);
	}

	@Override
	public final boolean isPaused()
	{
		return this.getState().equals(SchedulerJobState.PAUSED);
	}

	@Override
	public final boolean isFinished()
	{
		SchedulerJobState state = this.getState();
		return state.equals(SchedulerJobState.ABORTED) || this.getState().equals(SchedulerJobState.ERROR) || this.getState().equals(SchedulerJobState.SUCCESSFUL) || this.getState().equals(SchedulerJobState.TIMEOUT);

	}

	@Override
	public void waitForStates(SchedulerJobState... states)
	{
		waitForStates(-42, states);
	}

	@Override
	public void waitForStatesOtherThan(SchedulerJobState... states)
	{
		waitForStatesOtherThan(-42, states);
	}

	@Override
	public boolean waitForFinishing(long timeoutMillis)
	{
		return this.waitForStates(timeoutMillis, SchedulerJobState.ABORTED, SchedulerJobState.ERROR, SchedulerJobState.SUCCESSFUL, SchedulerJobState.TIMEOUT);
	}

	public boolean isState(SchedulerJobState... states)
	{
		for (SchedulerJobState s : states)
		{
			if (this.getState().equals(s))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean waitForStates(long timeoutMillis, SchedulerJobState... states)
	{
		long timestampWaitStarted = System.currentTimeMillis();
		log.debug("wait for job  ", this.getName(), "'s states: ", Arrays.asList(states), "; current state is: ", this.getState());
		while (true)
		{
			if ((timeoutMillis >= 0) && (System.currentTimeMillis() - timestampWaitStarted) > timeoutMillis)
			{
				log.debug("wait  job ", this.getName(), " timeout");
				return true;
			}
			if (this.isState(states))
			{
				return false;
			}
			this.sleep(sleepCycle);
		}
	}

	@Override
	public boolean waitForStatesOtherThan(long timeoutMillis, SchedulerJobState... states)
	{
		long timestampWaitStarted = System.currentTimeMillis();
		log.debug("wait job ", this.getName(), " for other job states than: ", Arrays.asList(states), "; current state is: ", this.getState());
		while (true)
		{
			if ((timeoutMillis >= 0) && (System.currentTimeMillis() - timestampWaitStarted) > timeoutMillis)
			{
				log.debug("wait job ", this.getName(), " timeout");
				return true;
			}
			if (!this.isState(states))
			{
				return false;
			}
			this.sleep(sleepCycle);
		}
	}

	public final void waitForFinishing()
	{
		this.waitForStates(SchedulerJobState.ABORTED, SchedulerJobState.ERROR, SchedulerJobState.SUCCESSFUL, SchedulerJobState.TIMEOUT);
	}

	@Override
	public final ISchedulerJobBase<T> setTimeout(long timeoutMillis)
	{
		this.jobTimeout = timeoutMillis;
		return this;
	}

	@Override
	public final ISchedulerJobBase<T> setSleepCycle(long sleepCycleMillis)
	{
		this.sleepCycle = sleepCycleMillis;
		return this;
	}

	@Override
	public final String getDescription()
	{
		return this.jobDescription;
	}

	@Override
	public final String getName()
	{
		return this.jobName;
	}

	@Override
	public final ISchedulerJobBase<T> setDescription(String description)
	{
		this.jobDescription = description;
		return this;
	}

	@Override
	public final ISchedulerJobBase<T> setName(String name)
	{
		this.jobName = name;
		return this;
	}

	/**
	 * checks for a pause interrupt; if it has been called, pauses the job until
	 * it is resumed. pausing is done with sleep cycles.
	 */
	public final void checkPause()
	{
		long pauseStartingTime = System.currentTimeMillis();
		if (!this.isRunning())
		{
			return;
		}
		if (this.pauseCalled)
		{
			log.debug("pausing job");
		}
		while (this.pauseCalled)
		{
			this.setJobState(SchedulerJobState.PAUSED);
			if ((this.pauseTime > -1) && ((System.currentTimeMillis() - pauseStartingTime) > this.pauseTime))
			{
				log.debug("pause timeout");
				break;
			}
			this.checkAbort();
			this.checkTimeout();
			this.sleep(sleepCycle);
		}
		this.resumeAsync();
		this.setJobState(SchedulerJobState.RUNNING);
		this.pauseTime = -1;
	}

	/**
	 * checks for a job timout; if it has occurred, does timeout procedures.
	 */
	public final void checkTimeout()
	{
		if ((!isRunning() && !isPaused()) || (this.timestampStartOfProcessing == -1) || (this.jobTimeout == -1))
		{
			return;
		}
		if ((System.currentTimeMillis() - this.timestampStartOfProcessing) > this.jobTimeout)
		{
			throw new SchedulerJobTimoutException();
		}
	}

	/**
	 * check for an abort interrupt; if it has been called, does abortion
	 * procedures.
	 */
	public final void checkAbort()
	{
		if (!isRunning() && !isPaused())
		{
			return;
		}
		if (this.abortCalled)
		{
			throw new SchedulerJobAbortionException();
		}
	}

	/**
	 * checks: 1. timeout 2. abort 3. pause
	 */
	public final void checkAllInterrupts()
	{
		checkTimeout();
		checkAbort();
		checkPause();
	}

	public void throwError(SchedulerJobError e)
	{
		this.jobError = e;
		throw new SchedulerJobErrorException();
	}

	public void success()
	{
		throw new SchedulerJobSuccessException();
	}

	public final IScheduler getScheduler()
	{
		return this.schedulder;
	}

	public final void setScheduler(Scheduler scheduler)
	{
		this.schedulder = scheduler;
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
}
