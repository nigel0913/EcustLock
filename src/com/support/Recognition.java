package com.support;

import android.util.Log;

public class Recognition {
	
	static 
	{
		Log.v ("loadLibrary", "start");
		System.loadLibrary("EcustLock");
		Log.v ("loadLibrary", "end");
	}
	
	private native static void jniTrainGmm(String worldPath, String feaPath, String mdlPath, String filename);
	private native static double jniTest(String worldPath, String feaPath, String mdlPath, String feaFile, String mdlFile);
	
	public static void TrainGmm(String worldPath, String feaPath, String mdlPath, String filename) {
		jniTrainGmm(worldPath, feaPath, mdlPath, filename);
	}
	public static double Test(String worldPath, String feaPath, String mdlPath, String feaFile, String mdlFile) {
		return jniTest(worldPath, feaPath, mdlPath, feaFile, mdlFile);
	}
	
	
}
