package com.zwb.scheduler.dispatcher.junit.util;

import java.util.ArrayList;
import java.util.List;

import com.zwb.scheduler.dispatcher.impl.DispatcherWorkerJobBase;
import com.zwb.scheduler.util.MyLogger;

public class SampleDispatcherWorkerJob01 extends DispatcherWorkerJobBase<ListOfInt, String>
{
	static long processingDelay = 0;
	static double processingDeviation = 0;
	String outSum = "";
	int sum = 0;
	boolean outSumFinished = false;
	String outProd = "";
	int prod = 1;
	boolean outProdFinished = false;
	private static MyLogger log = new MyLogger(SampleDispatcherWorkerJob01.class);

	
	public static void setProcessionDelay(long delay)
	{
		processingDelay = delay;
	}
	
	public static void setProcessionDeviation(double deviation)
	{
		processingDeviation = deviation;
	}
	
	@Override
	public void preProcess()
	{
		this.sleep(Util.createDelay(processingDelay, processingDeviation));
	}

	@Override
	public void postProcess()
	{
		writeBack("");
		this.sleep(Util.createDelay(processingDelay, processingDeviation));
	}

	@Override
	public void process()
	{
		log.debug("starting processing sum");
		for(int i=0; i<getInputData().list.size(); i++)
		{
			this.checkAllInterrupts();
			log.debug("...processing sum: ",i);
			sum += getInputData().list.get(i);
			outSum += getInputData().list.get(i).toString();
			if(i<(getInputData().list.size()-1))
			{
				outSum += "+";
			}
			else
			{
				outSum += "=";
				outSum += sum;
			}
			this.checkAllInterrupts();
			this.sleep(Util.createDelay(processingDelay, processingDeviation));
		}
		log.debug("outSumFinished");
		outSumFinished = true;

		log.debug("starting processing prod");
		for(int i=0; i<getInputData().list.size(); i++)
		{
			this.checkAllInterrupts();
			log.debug("...processing prod: ",i);
			prod *= getInputData().list.get(i);
			outProd += getInputData().list.get(i).toString();
			if(i<(getInputData().list.size()-1))
			{
				outProd += "*";
			}
			else
			{
				outProd += "=";
				outProd += prod;
			}
			this.checkAllInterrupts();
			this.sleep(Util.createDelay(processingDelay, processingDeviation));
		}
		log.debug("...outProdFinished: ");
		outProdFinished = true;
	}

	private void writeBack(String foo)
	{
		if(!outSumFinished)
		{
			outSum += "..."+foo;
		}
		this.addOutputData(outSum);
		if(!outProdFinished)
		{
			outProd += "..."+foo;
		}
		this.addOutputData(outProd);
	}
	
	@Override
	public void onError()
	{
		writeBack("error!");
	}

	@Override
	public void onTimeout()
	{
		writeBack("timeout!");
	}

	@Override
	public void onAbort()
	{
		writeBack("abort!");
	}
	
}
