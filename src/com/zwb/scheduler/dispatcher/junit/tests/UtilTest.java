package com.zwb.scheduler.dispatcher.junit.tests;

import com.zwb.scheduler.dispatcher.junit.util.Util;

import junit.framework.TestCase;

public class UtilTest extends TestCase
{
	public void testDeviation()
	{
		int cnt = 100000;
		long sum = 0;
		
		for (int i=0; i<cnt; i++)
		{
			long delay = Util.createDelay(100, 0.25);
			assertTrue(delay<=125);
			assertTrue(delay>=75);
			sum += delay;
		}
		long mean = sum/cnt;
		assertEqualsFuzzy(100, mean, 0.02);
	}

	private void assertEqualsFuzzy(double expected, double actual, double deviation)
	{
		assertEquals(expected, actual, (expected*deviation));
	}
}
