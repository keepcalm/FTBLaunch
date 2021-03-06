package net.ftb.updater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ftb.log.Logger;
import net.ftb.util.FileUtils;
import net.ftb.util.OSUtils;

/*
 Why is this necessary? You can't overwrite the currently running
 launcher, so you put the new copy in the temp directory and then
 it runs this as the main class there. This then copies itself
 over the old jar, and runs the old jar's main class from the
 correct location.
 */
public class SelfUpdate {
	public static void runUpdate(String currentPath, String temporaryUpdatePath) {
		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin"
				+ separator + "java";
		arguments.add(path);
		arguments.add("-cp");
		arguments.add(temporaryUpdatePath);
		arguments.add(SelfUpdate.class.getCanonicalName());
		arguments.add(currentPath);
		arguments.add(temporaryUpdatePath);

		Logger.logInfo("Would update with: " + arguments);
		Logger.logInfo("c: " + currentPath);
		Logger.logInfo("n: " + temporaryUpdatePath);
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(arguments);
		try {
			processBuilder.start();
		} catch (IOException e) {
			Logger.logError("Failed to start self-update process", e);
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		/*
		The way the arguments are used here MUST NOT CHANGE,
		unless you take care to ensure that auto-updating from
		an old version will still work - you can't just change how
		runUpdate() calls it, because the runUpdate() calling this
		will be old!
		 */
		try {
			if (OSUtils.getCurrentOS() != OSUtils.OS.UNIX) {
				Thread.sleep(4000); //Necessary as the updater may try to
				// run while the previous instance is still closing
				// preventing it from replacing the open launcher
				// Not necessary with linux, as you can replace currently
				// running files
			}
		} catch (InterruptedException ignored) { }
		String launcherPath = args[0];
		String temporaryUpdatePath = args[1];
		File launcher = new File(launcherPath);
		File temporaryUpdate = new File(temporaryUpdatePath);
		try {
			FileUtils.delete(launcher);
			FileUtils.copyFile(temporaryUpdate, launcher);
		} catch (IOException e) {
			System.out.print("Auto updating failed!");
			e.printStackTrace();
		}

		List<String> arguments = new ArrayList<String>();

		String separator = System.getProperty("file.separator");
		String path = System.getProperty("java.home") + separator + "bin"
				+ separator + "java";
		arguments.add(path);
		arguments.add("-jar");
		arguments.add(temporaryUpdatePath);

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(arguments);
		try {
			processBuilder.start();
		} catch (IOException e) {
			System.out.print("Failed to start launcher process after updating");
			e.printStackTrace();
		}
	}
}
