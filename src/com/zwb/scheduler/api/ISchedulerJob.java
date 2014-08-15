package com.zwb.scheduler.api;

public interface ISchedulerJob<T> extends ISchedulerJobBase<T>
{
	/**
	 * prepares the job. called by dispatcher in dispatcher thread context
	 * before work.
	 */
	public void preProcess();

	/**
	 * post-procedures after successful processing, freeing of resources. called
	 * by dispatcher in dispatcher thread context after work.
	 */
	public void postProcess();

	/**
	 * the jobs main working method. executed concurrently in own thread.
	 */
	public void process();

	/**
	 * function hook executed after an error occurred, for freeing of resources
	 * etc. called by dispatcher in dispatcher thread context after work.
	 */
	public void onError();

	/**
	 * function hook executed after a timeout occurred, for freeing of resources
	 * etc. called by dispatcher in dispatcher thread context after work.
	 */
	public void onTimeout();

	/**
	 * function hook executed after an abortion occurred, for freeing of resources
	 * etc. called by dispatcher in dispatcher thread context after work.
	 */
	public void onAbort();


}
