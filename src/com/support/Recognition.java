package com.support;

import android.util.Log;

public class Recognition {
	
	static 
	{
		Log.v ("loadLibrary", "start");
		System.loadLibrary("EcustLock");
		Log.v ("loadLibrary", "end");
	}
	
	private native static void writeFile(String inFile, String outFile);
	private native static double reco(String rootPath, String filename);
	public static void getMfcc(String inFile, String outFile) {
		writeFile(inFile, outFile);
	}
	public static double recognition(String rootPath, String filename)
	{
		Log.v("recognition in", "start");
		double tmp = reco(rootPath, filename);
		Log.v("recognition in", "end "+ tmp);
		return tmp;
	}
	
}
