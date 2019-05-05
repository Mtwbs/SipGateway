package com.owera.common.log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/*
 * Requirements
 * 
 * 1. Log to console, file and database
 * 2. Log-output format can be specified (not as rich as log4j)
 * 3. Log-rotation can be specified
 * 4. Severity levels align with syslog levels
 * 		- debug, info, notice, warn, error, crit/fatal, alert, emerg
 * 5. Log will find necessary propertyfiles in classpath
 * 6. Log hierarchy will be the same as in log4j
 * 7. Should be possible to integrate with log4j  
 */

public class Log {

	private final static String DEBUG_STR = "DEBUG ";
	private final static String INFO_STR = "INFO  ";
	private final static String NOTICE_STR = "NOTICE";
	private final static String WARN_STR = "WARN  ";
	private final static String ERROR_STR = "ERROR ";
	private final static String CRITIC_STR = "CRITIC";
	private final static String FATAL_STR = "FATAL ";
	private final static String ALERT_STR = "ALERT ";
	private final static String EMERG_STR = "EMERG ";

	public final static int DEBUG_INT = 7;
	public final static int INFO_INT = 6;
	public final static int NOTICE_INT = 5;
	public final static int WARN_INT = 4;
	public final static int ERROR_INT = 3;
	public final static int CRITIC_INT = 2;
	public final static int FATAL_INT = 2;
	public final static int ALERT_INT = 1;
	public final static int EMERG_INT = 0;

	public final static String[] PADS = new String[] { " ", "  ", "   ", "    ", "     ", "      ", "       ", "        ", "         ", "          " };

	private final static Map<String, Integer> severityStr2IntMap = new HashMap<String, Integer>();
	private final static Map<Integer, String> severityInt2StrMap = new HashMap<Integer, String>();

	static {
		severityStr2IntMap.put(DEBUG_STR, DEBUG_INT);
		severityStr2IntMap.put(INFO_STR, INFO_INT);
		severityStr2IntMap.put(NOTICE_STR, NOTICE_INT);
		severityStr2IntMap.put(WARN_STR, WARN_INT);
		severityStr2IntMap.put(ERROR_STR, ERROR_INT);
		severityStr2IntMap.put(CRITIC_STR, CRITIC_INT);
		severityStr2IntMap.put(FATAL_STR, FATAL_INT);
		severityStr2IntMap.put(ALERT_STR, ALERT_INT);
		severityStr2IntMap.put(EMERG_STR, EMERG_INT);
		severityInt2StrMap.put(DEBUG_INT, DEBUG_STR);
		severityInt2StrMap.put(INFO_INT, INFO_STR);
		severityInt2StrMap.put(NOTICE_INT, NOTICE_STR);
		severityInt2StrMap.put(WARN_INT, WARN_STR);
		severityInt2StrMap.put(ERROR_INT, ERROR_STR);
		severityInt2StrMap.put(CRITIC_INT, FATAL_STR);
		severityInt2StrMap.put(FATAL_INT, FATAL_STR);
		severityInt2StrMap.put(ALERT_INT, ALERT_STR);
		severityInt2StrMap.put(EMERG_INT, EMERG_STR);
	}

	
	private static Map<String, Appender> appenderMap = new Hashtable<String, Appender>();
	protected static String DEFAULT_APPENDER_NAME = "DEFAULT-APPENDER";
	private static int DEFAULT_APPENDER_LOGLEVEL = DEBUG_INT;
	private static boolean cheapInit = false;
	private static long lastSecond = 0;
	private static Calendar cal = Calendar.getInstance();
	private static String thisSecondStr;
	private static Object syncMonitor = new Object();

	


	

	/*
	 * This method initializes the log framework properly. Using this
	 * method you can customize all parts of the logging using a 
	 * propertyfile.
	 */


	/*
	 * This is the cheap way of initializing the log framework. Using
	 * this method you will be able to control the overall loglevel, but
	 * everything else is not customizable. Thus everything will be logged
	 * to stdout, that is at the correct loglevel. 
	 */
	public static void initialize(int logLevel) {
		cheapInit = true;
		DEFAULT_APPENDER_LOGLEVEL = logLevel;
	}

	private static void pad(StringBuilder sb, String appendStr, int size) {
		sb.append(appendStr);
		int padsToGo = size - appendStr.length();
		if (padsToGo > 0) {
			while (padsToGo > 10) {
				sb.append(PADS[9]);
				padsToGo -= 10;
			}
			sb.append(PADS[padsToGo - 1]);
		}
	}

	

	private static void normalize(StringBuilder sb, int number, String trailingChar) {
		if (number < 10)
			sb.append("0");
		sb.append(number);
		sb.append(trailingChar);
	}

	protected static void computeDate(LogObject lo) {
		long now = System.currentTimeMillis();
		long currentSecond = now / 1000;
		if (currentSecond != lastSecond) {
			StringBuilder currentSecondStr = new StringBuilder();
			cal.setTimeInMillis(now);
			normalize(currentSecondStr, cal.get(Calendar.YEAR), "-");
			normalize(currentSecondStr, cal.get(Calendar.MONTH) + 1, "-");
			normalize(currentSecondStr, cal.get(Calendar.DAY_OF_MONTH), " ");
			normalize(currentSecondStr, cal.get(Calendar.HOUR_OF_DAY), ":");
			normalize(currentSecondStr, cal.get(Calendar.MINUTE), ":");
			normalize(currentSecondStr, cal.get(Calendar.SECOND), ".");
			thisSecondStr = currentSecondStr.toString();
			lastSecond = currentSecond;
		}
		lo.setTms(now);
		lo.setCurrentSecond(currentSecond);
		String millis = "" + now % 1000;
		while (millis.length() < 3)
			millis = "0" + millis;
		lo.setTimestampStr(thisSecondStr + millis);
		//		lo.setTimestampStr("1001-01-01 23:23:23.949");
	}

	public static long getCurrentSecond() {
		return System.currentTimeMillis() / 1000;
	}

	
}