package com.mecool.util;


import org.slf4j.LoggerFactory;

public class Logger {

	private org.slf4j.Logger logger;

	
	/**
	 * 构造方法，初始化Log4j的日志对象
	 */
	private Logger(org.slf4j.Logger log4jLogger) {
		logger = log4jLogger;
	}

	/**
	 * 获取构造器，根据类初始化Logger对象
	 * 
	 * @param Class
	 *            Class对象
	 * @return Logger对象
	 */
	public static Logger getLogger(Class classObject) {
		return new Logger(LoggerFactory.getLogger(classObject));
	}

	/**
	 * 获取构造器，根据类名初始化Logger对象
	 * 
	 * @param String
	 *            类名字符串
	 * @return Logger对象
	 */
	public static Logger getLogger(String loggerName) {
		return new Logger(LoggerFactory.getLogger(loggerName));
	}

	public void debug(Object object) {
		logger.debug(object.toString());
	}

	public void debug(Object object, Throwable e) {
		logger.debug(object.toString(), e);
	}

	public void info(Object object) {
		logger.info(object.toString());
	}

	public void info(Object object, Throwable e) {
		logger.info(object.toString(), e);
	}

	public void warn(Object object) {
		logger.warn(object.toString());
	}

	public void warn(Object object, Throwable e) {
		logger.warn(object.toString(), e);
	}

	public void error(Object object) {
		logger.error(object.toString());
	}

	public void error(Object object, Throwable e) {
		logger.error(object.toString(), e);
	}

	public String getName() {
		return logger.getName();
	}

	public org.slf4j.Logger getLog4jLogger() {
		return logger;
	}

	public boolean equals(Logger newLogger) {
		return logger.equals(newLogger.getLog4jLogger());
	}
}