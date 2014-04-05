package com.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class SRecord {

	private static SRecord instance;
	private SRecord() {}
	public static SRecord getInstance() {
		if (instance == null) {
			instance = new SRecord();
		}
		return instance;
	}
	
	/**
	 * 创建Record对象
	 * @return EResultType.SUCCESS, EResultType.FAILED
	 */
	public EResultType createRecord() {
		bufferSizeInBytes =  AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
		if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			this.isCreate = true;
			return EResultType.SUCCESS;
		}
		this.isCreate = false;
		return EResultType.FAILED;
	}
	/**
	 * 释放Record资源
	 */
	public void releaseRecord() {
		if (audioRecord != null) {			
			audioRecord.release();
			audioRecord = null;		
			this.isCreate = false;
		}
	}
	
	/**
	 * 开始录音
	 */
	public void startRecord() {
		Log.v(tag, "startRecord start");
		audioRecord.startRecording();
		isRecording = true;
		new Thread(new AudioRecordThread()).start();
		Log.v(tag, "startRecord end");
	}
	/**
	 * 结束录音
	 */
	public void closeRecord() {
		if (audioRecord != null) {
			isRecording = false;
			audioRecord.stop();
		}
	}

	
	class AudioRecordThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.v(tag, "AudioRecordThread start");
			writeRecord();
			Log.v(tag, "AudioRecordThread end");
		}
		
	}
	
	void writeRecord() {
		byte[] audioData = new byte[bufferSizeInBytes];
		FileOutputStream fos = null;
		int readsize = 0;
		try {
			File file = new File(dirs + File.separator + fileName + suffix);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (isRecording == true) {
			readsize = audioRecord.read(audioData, 0, bufferSizeInBytes);
			if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
				try {
					fos.write(audioData);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置目录
	 * @param dirs
	 */
	public void setDirs(String dirs) {
		this.dirs = dirs;
	}
	/**
	 * 设置文件名
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * 设置后缀名
	 * @param suffix
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getDirs() {
		return dirs;
	}
	public boolean isRecording() {
		return isRecording;
	}
	public String getFileName() {
		return fileName;
	}
	public String getSuffix() {
		return suffix;
	}
	
	public boolean isCreate() {
		return isCreate;
	}
	
	/**
	 * 音频获取源
	 */
	int audioSource = MediaRecorder.AudioSource.MIC;
	
	/**
	 * 频率
	 */
	static int sampleRateInHz = 8000;
	
	/**
	 * 单声道
	 */
	static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	
	/**
	 * 一个采样点16比特-2个字节
	 */
	static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	/**
	 * 缓冲区大小
	 */
	int bufferSizeInBytes = 0;
	
	boolean isRecording = false;
	
	boolean isCreate = false;



	/**
	 * 文件后缀名
	 */
	String suffix = ".raw";
	
	/**
	 * 文件名
	 */
	String fileName = "";
	
	/**
	 * 目录名
	 * @sample ./sdcard
	 */
	String dirs = ".";
	AudioRecord audioRecord = null;
	

	String tag = "SRecord";
	
	public enum EResultType {
		SUCCESS,
		FAILED,
	}
	
}
