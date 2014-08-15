package com.zwb.scheduler.junit.util;

import java.util.ArrayList;
import java.util.List;

import com.zwb.scheduler.impl.Scheduler;
import com.zwb.scheduler.impl.SchedulerJobBase;

public class LoadMonitor extends SchedulerJobBase<List<LoadDataSet>>
{
	private long startingTimestamp;
	private long timeGranularity;

	public LoadMonitor(long timeGranularity)
	{
		this.startingTimestamp = System.currentTimeMillis();
		this.timeGranularity = timeGranularity;
		this.setSleepCycle(300);
		this.setName("LoadMonitor");
	}

	public String getLoadTable()
	{
		String s = "";

		double cntRegistered = 0;
		double cntRunnning = 0;
		double cntWaiting = 0;
		double cnt = 0;

		s += "-------------------------------------------------------------\n";
		s += "time [s]" + "\t# waiting" + "\t# running\n";
		s += "-------------------------------------------------------------\n";
		for (LoadDataSet l : this.getResult())
		{
			cnt++;
			cntRunnning += l.noOfJobsRunning;
			cntWaiting += l.noOfJobsWaiting;
			s += l.timestamp / 1000.0 + "\t\t" + l.noOfJobsWaiting + "\t\t" + l.noOfJobsRunning + "\n";
		}
		s += "-------------------------------------------------------------\n";
		s += "# of Measurements : " + cnt + "\n";
		s += "Mean Registered   : " + cntRegistered / cnt + "\n";
		s += "Mean Waiting      : " + cntWaiting / cnt + "\n";
		s += "Mean Running      : " + cntRunnning / cnt + "\n";
		s += "Mean Running      : " + getMeanRunningWhileAtLeastOneWaiting() + "(while at least one waiting) \n";
		s += "-------------------------------------------------------------\n";
		return s;
	}

	public double getMeanRunning()
	{
		double cntRunnning = 0;
		double cnt = 0;
		for (LoadDataSet l : this.getResult())
		{
			cnt++;
			cntRunnning += l.noOfJobsRunning;
		}
		return cntRunnning / cnt;
	}

	public double getMeanRunningWhileAtLeastOneWaiting()
	{
		double cntRunnning = 0;
		double cnt = 0;
		for (LoadDataSet l : this.getResult())
		{
			if (l.noOfJobsWaiting > 0)
			{
				cnt++;
				cntRunnning += l.noOfJobsRunning;
			}
		}
		return cntRunnning / cnt;
	}

	@Override
	public void preProcess()
	{
		this.setResult(new ArrayList<LoadDataSet>());
	}

	@Override
	public void postProcess()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void process()
	{
		while (true)
		{
			this.checkAllInterrupts();
			Scheduler d = (Scheduler) this.getScheduler();
			this.getResult().add(new LoadDataSet(this.startingTimestamp, d.getNumberObJobsWaiting(), d.getNumberObJobsRunning()));
			this.sleep(this.timeGranularity);
		}
	}

	@Override
	public void onError()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onTimeout()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onAbort()
	{
		// TODO Auto-generated method stub
	}
}
