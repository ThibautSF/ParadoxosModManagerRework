package com.pmm.ParadoxosGameModManager.debug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * @author SIMON-FINE Thibaut (alias Bisougai)
 *
 */
public class ErrorPrint {
	private static File debugFile = new File("DebugLog.txt");
	private static BufferedWriter writer;

	/**
	 * @param e
	 */
	public static void printError(Exception e) {
		printError(e, null);
	}

	/**
	 * @param e
	 * @param str
	 */
	public static void printError(Exception e, String str) {
		try {
			writer = new BufferedWriter(new FileWriter(debugFile, true));
			writeFile("");
			writeInfo("EXCEPTION MESSAGE");
			if (str != null) {
				writeFile("Action : " + str);
			}

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String exceptionText = sw.toString();

			writeFile(exceptionText);
			writer.close();
		} catch (Exception eF) {
			eF.printStackTrace();
		}
	}

	/**
	 * @param str
	 */
	public static void printError(String str) {
		try {
			writer = new BufferedWriter(new FileWriter(debugFile, true));
			writeFile("");
			writeInfo("ERROR MESSAGE");
			writeFile(str);
			writer.close();
		} catch (Exception eF) {
			eF.printStackTrace();
		}
	}

	/**
	 * @param infoType
	 */
	private static void writeInfo(String infoType) {
		LocalDateTime now = LocalDateTime.now();
		int year = now.getYear();
		int month = now.getMonthValue();
		int day = now.getDayOfMonth();
		int hour = now.getHour();
		int minute = now.getMinute();
		int second = now.getSecond();
		String strDate = String.format("%d-%02d-%02d %02d:%02d:%02d : type %s", year, month, day, hour, minute, second,
				infoType);
		writeFile(strDate);
	}

	/**
	 * @param strToWrite
	 */
	private static void writeFile(String strToWrite) {
		try {
			writer.write(strToWrite + System.getProperty("line.separator"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
