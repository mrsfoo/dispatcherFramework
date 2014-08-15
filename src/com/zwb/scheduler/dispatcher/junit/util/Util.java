package com.zwb.scheduler.dispatcher.junit.util;

public class Util
{

	public static long createDelay(long base, double deviation)
	{
		return (long)(((double)base) * (1.0+(Math.random()*deviation*2.0)-deviation));
	}
	
}
