package com.support;

//√ª”–”√

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.util.Log;

public class Test {
	
	String trainDirPath;
	String authDirPath;
	String authFileName;
	String authLog;
	
	FileOutputStream fos = null;

	public Test() {
		trainDirPath = null;
		authDirPath = authFileName = null;
		authLog = null;
	}
	
	/**
	 * initial a new train directory
	 * @param root sdcard/.../raw
	 */
	public void startBackupTrain(String root) {
		trainDirPath = root + File.separator + "Train_" + getTimeStamp();
		File dir = new File( trainDirPath );
		if ( !dir.exists() ) {
			dir.mkdir();
		}
	}
	
	/**
	 * inital a new authenticate file name
	 * @param root
	 */
	public void startBackupAuth(String root) {
		authFileName = "Auth_" + getTimeStamp();
		authDirPath = root + File.separator + authFileName;
		Log.v("auth",authDirPath + " " + authFileName);
		File dir = new File( authDirPath );
		if ( !dir.exists() ) {
			dir.mkdir();
		}
		authLog = root + File.separator + "auth.log";
	}
	
	public String getTrainDirPath() {
		return trainDirPath;
	}
	public String getAuthDirPath() {
		return authDirPath;
	}
	
	public void backupLog(double score) {
		try {
			fos = new FileOutputStream(authLog, true);
			PrintWriter writer = new PrintWriter(fos);
			writer.println(authFileName + ": " + score);
			writer.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void backupShortData(String fileName, short[] data, int len) {
		
		try {
			fos = new FileOutputStream(fileName, true);
			
			byte[] tmp = new byte[2];
			for (int i=0; i<len; i++) {
				for (int j=0; j<2; j++) {
					tmp[j] = (byte) (data[i] >> (8*j) & 0xff);
				}
				fos.write(tmp);
			}
			
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void backupFloatData(String fileName, float[] data, int len) {
		
		try {
			fos = new FileOutputStream(fileName, true);
			
			byte[] tmp = new byte[4];
			for (int i=0; i<len; i++) {
				int dataInt = Float.floatToIntBits(data[i]);
				for (int j=0; j<4; j++) {
					tmp[j] = (byte) ((dataInt >> (8*j)) & 0xff);
				}
				fos.write(tmp);
			}
			
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return time stamp
	 */
	private String getTimeStamp() {
		return new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
	
}
