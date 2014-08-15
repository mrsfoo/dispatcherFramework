package com.zwb.scheduler.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.zwb.scheduler.impl.Scheduler;

public class MyLogger
{
	private Logger logger;

	public enum LogLevel
	{
		OFF, TRACE, DEBUG, INFO, WARN, ERROR, FATAL, ALL
	};

	public MyLogger(Class<?> clazz)
	{
		logger = Logger.getLogger(clazz);
		PropertyConfigurator.configure("config/log4j.properties");
	}

	private Level mapLogLevel(LogLevel level)
	{
		switch (level)
		{
		case DEBUG:
			return Level.DEBUG;
		case ERROR:
			return Level.ERROR;
		case FATAL:
			return Level.FATAL;
		case INFO:
			return Level.INFO;
		case TRACE:
			return Level.TRACE;
		case WARN:
			return Level.WARN;
		case ALL:
			return Level.ALL;
		case OFF:
			return Level.OFF;
		default:
			return null;
		}
	}

	public void log(LogLevel level, Object... messages)
	{
		Level log4jLevel = mapLogLevel(level);
		if ((log4jLevel == null) || (!logger.isEnabledFor(log4jLevel)))
		{
			return;
		}
		StringBuilder msg = new StringBuilder();
		for (Object o : messages)
		{
			msg.append(o);
		}
		this.logger.log(log4jLevel, msg);
	}

	public void trace(Object... messages)
	{
		this.log(LogLevel.TRACE, messages);
	}

	public void debug(Object... messages)
	{
		this.log(LogLevel.DEBUG, messages);
	}

	public void info(Object... messages)
	{
		this.log(LogLevel.INFO, messages);
	}

	public void warn(Object... messages)
	{
		this.log(LogLevel.WARN, messages);
	}

	public void error(Object... messages)
	{
		this.log(LogLevel.ERROR, messages);
	}

	public void fatal(Object... messages)
	{
		this.log(LogLevel.FATAL, messages);
	}

}
