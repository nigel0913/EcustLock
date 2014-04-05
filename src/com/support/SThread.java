package com.support;

import java.io.File;

import com.support.Config.DOTYPE;

import android.util.Log;

public class SThread {

	public void getMfcc() {
		Thread th = new Thread( new GetMfccThread() );
		th.setPriority(Thread.MIN_PRIORITY);
		th.start();
	}
	
	class GetMfccThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String iFile = Config.getRootDir()  + Config.getRawPath() + File.separator +
					Config.getUserName() + Config.getRawSuf();
			String oFile;
			if (Config.getType() == DOTYPE.TRAIN){				
				oFile = Config.getRootDir()  + Config.getFeaturePath() + File.separator +
						Config.getUserName() + Config.getFeaSuf();
			}
			else {
				oFile = Config.getRootDir()  + Config.getTestFeaturePath() + File.separator +
						Config.getUserName() + Config.getFeaSuf();
			}

			Log.v("getMfcc", "start\n"+iFile+"\n"+oFile);
//			RecognitionTest.getMfcc(iFile, oFile);
			Log.v("getMfcc", "end");
		}
		
	}
}
