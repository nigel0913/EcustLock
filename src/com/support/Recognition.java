package com.support;

import android.util.Log;

public class Recognition {
	
	static 
	{
		Log.v ("loadLibrary", "start");
		System.loadLibrary("EcustLock");
		Log.v ("loadLibrary", "end");
	}
	
	private native static void jniTrainGmm(String rootPath, String filename);
	private native static double jniTest(String rootPath, String filename);
	
	public static void TrainGmm(String rootPath, String filename) {
		jniTrainGmm(rootPath, filename);
	}
	public static double Test(String rootPath, String filename) {
		return jniTest(rootPath, filename);
	}
	
	
}
