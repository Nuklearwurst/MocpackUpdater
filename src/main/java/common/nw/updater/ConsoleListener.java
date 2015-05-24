package common.nw.updater;

import common.nw.updater.gui.IProgressWatcher;
import common.nw.utils.log.NwLogger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ConsoleListener implements IProgressWatcher {

	@Override
	public boolean isCancelled() {
		return false;
	}
	
	@Override
	public boolean isPaused() {
		return false;
	}

	@Override
	public void setDownloadProgress(String msg) {
		Updater.logger.info("Download Progress: " + msg);
	}

	@Override
	public void setDownloadProgress(int progress) {
	}

	@Override
	public void setDownloadProgress(String msg, int progress) {
		setDownloadProgress(msg);
	}

	@Override
	public void setDownloadProgress(String msg, int progress, int maxProgress) {
		setDownloadProgress(msg);
	}

	@Override
	public void setOverallProgress(int progress) {
	}

	@Override
	public void setOverallProgress(String msg, int progress) {
		Updater.logger.info("Overall Progress: " + msg);
	}

	@Override
	public boolean quitToLauncher() {
		return false;
	}

	@Override
	public int showErrorDialog(String title, String message) {
		String[] options = { "Retry", "Quit To Launcher", "Continue without updating" };
		return showOptionDialog(message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
	}

	@Override
	public int showConfirmDialog(String message, String title, int optionType,
			int messageType) {
		System.out.println("[" + getStringForMessageType(messageType) + " - confirm] " + title);
		System.out.println(message);
		if(optionType == JOptionPane.YES_NO_OPTION) {
			System.out.println("######   y / n   ######");			
		} else {
			System.out.println("######   y / n / c  ######");
		}
		String s = readLine();
		if(s != null && s.toLowerCase().equals("y")) {
			return JOptionPane.YES_OPTION;
		}
		if(s != null && s.toLowerCase().equals("n")) {
			return JOptionPane.NO_OPTION;
		}
		return optionType == JOptionPane.YES_NO_CANCEL_OPTION ? JOptionPane.CANCEL_OPTION : JOptionPane.NO_OPTION;
	}

	@Override
	public String showInputDialog(String message) {
		System.out.println("[Message - input] " + message);
		return readLine();
	}

	@Override
	public int showOptionDialog(String message, String title, int optionType,
			int messageType, Icon icon, String[] options, String defaultOption) {
		System.out.println("[" + getStringForMessageType(messageType) + " - confirm] " + title);
		System.out.println(message);

		//try 3 times
		for(int t = 0; t < 3; t++) {

			//print options
			System.out.println();
			for(int i = 0; i < options.length; i++) {
				System.out.println("[" + i + "] " + options[i] + " ");
			}
			System.out.println();

			//read input
			String input_str = readLine();
			if(input_str == null) {
				continue;
			}

			for(int i = 0; i < options.length; i++) {
				String o = options[i];
				if(o != null && o.equals(input_str)) {
					return i;
				}
			}
			//read number
			//noinspection EmptyCatchBlock
			try {
				int input_int = Integer.parseInt(input_str);
				if(input_int < options.length && input_int >= 0) {
					return input_int;
				}
			} catch(NumberFormatException e) {}
			NwLogger.NW_LOGGER.error("Invalid input!");

		}
		//error handling (after 3 trys no result)
		if(defaultOption != null) {
			for(int i = 0; i < options.length; i++) {
				String s = options[i];
				if(s != null && s.equals(defaultOption)) {
					return i;
				}
			}
		}
		return 0;
	}

	private String getStringForMessageType(int m) {
		switch(m) {
		case JOptionPane.PLAIN_MESSAGE:
			return "Message";
		case JOptionPane.ERROR_MESSAGE:
			return "Error";
		case JOptionPane.WARNING_MESSAGE:
			return "Warning";
		case JOptionPane.QUESTION_MESSAGE:
			return "Question";
		case JOptionPane.INFORMATION_MESSAGE:
			return "Information";
		}
		return "InfoMsg";
	}

	private String readLine() {
		if(System.console() != null) {
			return System.console().readLine();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String s;
		try {
			s = reader.readLine();
		} catch (IOException e) {
			Scanner in = new Scanner(System.in);
			s = in.nextLine();   
			in.close();
		}
		return s;
	}
}
