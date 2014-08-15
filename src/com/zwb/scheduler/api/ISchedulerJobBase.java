package com.zwb.scheduler.api;

public interface ISchedulerJobBase<T>
{
	/**
	 * @return the jobs result, if executed; throws an exception if the job
	 *         hasn't been executed successfully
	 */
	public T getResult();

	/**
	 * sets the jobs result.
	 */
	public void setResult(T result);

	/**
	 * abort the job (abortion functionality must be implemented by the job)
	 * (synchronous execution).
	 */
	public void abortSync();

	/**
	 * abort the job (abortion functionality must be implemented by the job)
	 * (asynchronous execution).
	 */
	public void abortAsync();

	/**
	 * pause the job (pause functionality must be implemented by the job)
	 * (synchronous execution).
	 */
	public void pauseSync();

	/**
	 * pause the job (pause functionality must be implemented by the job)
	 * (asynchronous execution).
	 */
	public void pauseAsync();

	/**
	 * pause the job the passed number of millis (pause functionality must be
	 * implemented by the job) (synchronous execution).
	 */
	public void pauseSync(long pauseTimeoutMillis);

	/**
	 * pause the job the passed number of millis (pause functionality must be
	 * implemented by the job) (asynchronous execution).
	 */
	public void pauseAsync(long pauseTimeoutMillis);

	/**
	 * sets a job error and ends the job's execution
	 */
	public void throwError(SchedulerJobError e);

	/**
	 * sets a job successful
	 */
	public void success();

	/**
	 * resume the job if paused (synchronous execution).
	 */
	public void resumeSync();

	/**
	 * resume the job if paused (asynchronous execution).
	 */
	public void resumeAsync();

	/**
	 * @return the state of the job
	 */
	public SchedulerJobState getState();

	/**
	 * @return the last error that occurred in job execution; returns null if no
	 *         error occurred
	 */
	public SchedulerJobError getError();

	/**
	 * @return the time since the beginning of the job processing.
	 */
	public long getRuntime();

	/**
	 * @return the time that was spent in job pre-processing
	 */
	public long getPreProcessingDuration();

	/**
	 * @return the time that was spent in job post-processing
	 */
	public long getPostProcessingDuration();

	/**
	 * @return the time that was spent in job processing
	 */
	public long getProcessingDuration();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isInitialized();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isWaiting();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isRunning();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isSuccessful();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isAborted();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isError();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isTimeout();

	/**
	 * convenience method for checking the job state.
	 */
	public boolean isPaused();

	/**
	 * convenience method for checking the job state. finished is defined as:
	 * successful, timeout, aborted, error.
	 */
	public boolean isFinished();

	/**
	 * sets a timeout for the job, after which it is stopped; timeout checking
	 * must be implemented by the job.
	 */
	public ISchedulerJobBase<T> setTimeout(long timeoutMillis);

	/**
	 * sets the job's sleep cycle in millis if paused.
	 */
	public ISchedulerJobBase<T> setSleepCycle(long sleepCycleMillis);

	/**
	 * @return job description for logging/debugging
	 */
	public String getDescription();

	/**
	 * @return the name of the job for debugging/logging
	 */
	public String getName();

	/**
	 * sets the job description for logging/debugging
	 */
	public ISchedulerJobBase<T> setDescription(String description);

	/**
	 * sets the name of the job for debugging/logging
	 */
	public ISchedulerJobBase<T> setName(String name);

	/**
	 * waits until the job is not running anymore.
	 */
	public void waitForFinishing();

	/**
	 * waits until the job passed to one of the given states.
	 */
	public void waitForStates(SchedulerJobState... states);

	/**
	 * waits until the job passed to a state not in the list of the given
	 * states.
	 */
	public void waitForStatesOtherThan(SchedulerJobState... states);

	/**
	 * waits until the job is not running anymore or until timeout has elapsed.
	 * @returns true if timeout occurred, false otherwise
	 */
	public boolean waitForFinishing(long timeoutMillis);

	/**
	 * waits until the job passed to one of the given states or until timeout
	 * has elapsed.
	 * @returns true if timeout occurred, false otherwise
	 */
	public boolean waitForStates(long timeoutMillis, SchedulerJobState... states);

	/**
	 * waits until the job passed to a state not in the list of the given states
	 * or until timeout has elapsed.
	 * @returns true if timeout occurred, false otherwise
	 */
	public boolean waitForStatesOtherThan(long timeoutMillis, SchedulerJobState... states);
}
