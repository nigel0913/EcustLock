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
	 * ����Record����
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
	 * �ͷ�Record��Դ
	 */
	public void releaseRecord() {
		if (audioRecord != null) {			
			audioRecord.release();
			audioRecord = null;		
			this.isCreate = false;
		}
	}
	
	/**
	 * ��ʼ¼��
	 */
	public void startRecord() {
		Log.v(tag, "startRecord start");
		audioRecord.startRecording();
		isRecording = true;
		new Thread(new AudioRecordThread()).start();
		Log.v(tag, "startRecord end");
	}
	/**
	 * ����¼��
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
	 * ����Ŀ¼
	 * @param dirs
	 */
	public void setDirs(String dirs) {
		this.dirs = dirs;
	}
	/**
	 * �����ļ���
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * ���ú�׺��
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
	 * ��Ƶ��ȡԴ
	 */
	int audioSource = MediaRecorder.AudioSource.MIC;
	
	/**
	 * Ƶ��
	 */
	static int sampleRateInHz = 8000;
	
	/**
	 * ������
	 */
	static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	
	/**
	 * һ��������16����-2���ֽ�
	 */
	static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	/**
	 * ��������С
	 */
	int bufferSizeInBytes = 0;
	
	boolean isRecording = false;
	
	boolean isCreate = false;



	/**
	 * �ļ���׺��
	 */
	String suffix = ".raw";
	
	/**
	 * �ļ���
	 */
	String fileName = "";
	
	/**
	 * Ŀ¼��
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
