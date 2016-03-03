package com.paul.utils.log;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger {

	public enum Level {
		NONE, DEBUG, INFO, ERROR
	};

	private static SimpleLogger instance = null;
	private Level loggingLevel = Level.NONE;

	private File logFile = null;
	private FileWriter fileWriter = null;
	private BufferedWriter bufferedWriter = null;
	private PrintWriter printWriter = null;

	private boolean logToWindow = false;
	private boolean logFileCreated = false;

	protected SimpleLogger() {}

	public static SimpleLogger getInstance() {
		if (instance == null) {
			instance = new SimpleLogger();
		}
		return instance;
	}

	public void setLevel(Level level) {
		loggingLevel = level;
	}

	public void logMessage(Level level, String message) {
		logMessage(level, message, null);
	}

	public void logMessage(Level level, String msg, Exception ex) {

		// no logging
		if (!(loggingLevel.equals(Level.NONE))) {
			
			if (!logFileCreated && !logToWindow) {
				createLogFile();
			}

			// regardless of logging level, always log
			if ((loggingLevel.equals(Level.DEBUG))
					|| (loggingLevel.equals(Level.INFO) && (level.equals(Level.INFO) || level.equals(Level.ERROR)))
					|| (loggingLevel.equals(Level.ERROR) && level.equals(Level.ERROR))) {
				writeMessage(level, msg, ex);
			}
		}
	}

	private void writeMessage(Level level, String msg, Exception ex) {

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
		String dateString = dateFormat.format(date);

		try {

			if (logToWindow) {
				if (ex == null) {
					System.err.format("%s %s: Message %s%n", dateString, level, msg);
				} else {
					System.err.format("%s %s: Message %s%n Exception: %s", dateString, level, msg, ex);
				}
			} else {
				if (ex == null) {
					printWriter.printf("%s %s: Message %s%n", dateString, level, msg);
				} else {
					printWriter.printf("%s %s: Message %s%n Exception: %s", dateString, level, msg, ex);
				}
			}
		} catch (Exception e) {
			System.err.format("ERROR: Unable to write to log file: %s%n", e);
		}

	}
	
	protected void createLogFile() {
		
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSSZ");
		String fileName = dateFormat.format(date) + ".txt";

		try {
				logFile = new File(fileName);
				logFile.createNewFile();
				fileWriter = new FileWriter(logFile);
				bufferedWriter = new BufferedWriter(fileWriter);
				printWriter = new PrintWriter(bufferedWriter, true);
				logFileCreated = true;

		} catch (IOException e) {
			logToWindow = true;
			System.err.format("ERROR: Unable to Create log file: %s%n", e);
		}

	}

	protected void finalize() {
		if (printWriter != null) {
			printWriter.close();
		}

	}

}
