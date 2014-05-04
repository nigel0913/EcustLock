package com.support;

import java.io.File;

public class FileAccess {

	public static boolean Move(String srcFile, String destPath) {
		File src = new File(srcFile);
		File dir = new File(destPath);

		boolean result = src.renameTo(new File(dir, src.getName()));

		return result;
	}

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

}
