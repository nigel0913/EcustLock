package com.support;

import java.io.File;

public class FileAccess {

	public static boolean Move(String srcFile, String destPath)
	{
		File src = new File(srcFile);
		File dir = new File(destPath);
		
		boolean result = src.renameTo(new File(dir, src.getName()));
		
		return result;
	}
	
}
