package com.support.mfcc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Mfcc {

	static int dim = 26;
	static int p = 24;
	static int fs = 8000;
	static int frame = 256;
	
	static double[][] dct = new double[dim/2+1][p+1];
	static double[] w = new double[dim/2+1];
	static double[] c1 = new double[dim/2+1];
	static double[][] bank = null;
	static double[] hammingwin = null;
	static int bankH = 0;
	static int bankW = 0;
	
	FileOutputStream fos = null;
	
	static double[] buffer = new double[frame * 4 + 1];
	static int bufferSize = 0;
	
	int mfccNum = 0;
	
	public static Mfcc getInstance() {
		return INSTANCE;
	}
	
	private static Mfcc INSTANCE = new Mfcc();
	private Mfcc() {
		for (int i = 0; i < frame * 4; i++) {
			buffer[i] = 0;
		}
		
		hammingwin = hamming(frame);
		
		bank = MelBankm.melbankm(p, frame, fs, 0, 0.5);
		
		int fn2 = (int) Math.floor( frame / 2 );
		// bank = melbankm(24, 256, fs, 0, 0.5, 'm');
		// bank = full(bank);
		// bank = bank/max(bank(:));
		bankH = p;
		bankW = fn2 + 1;
		
		double bankMax = -1e30;
		for (int i = 1; i <= bankH; i++) {
			for (int j = 1; j <= bankW; j++) {
				bankMax = Math.max(bankMax, bank[i][j]);
			}
		}
		for (int i = 1; i <= bankH; i++) {
			for (int j = 1; j <= bankW; j++) {
				bank[i][j] = bank[i][j] / bankMax;
			}
		}
		
		for (int k = 1; k <= dim/2; k++) {
			for (int n = 0; n < p; n++) {
				dct[k][n+1] = Math.cos( (2 * n + 1) * k * Math.PI / (2 * p) );
			}
		}
		
		double maxW = -1e30;
		for (int i = 1; i <= dim/2; i++) {
			w[i] = 1 + 6 * Math.sin(Math.PI * i / (dim/2));
			maxW = Math.max(maxW, w[i]);
		}
		for (int i = 1; i <= dim/2; i++) {
			w[i] = w[i] / maxW;
		}
	}
	
	/**
	 * @brief the main entrance method
	 * @param data subscript start from 1
	 * @param len
	 */
	public void write(File file, double[] data, int len) {
		int xlen = bufferSize + len;
		double[] x = new double[xlen + 1];
		for (int i = 1; i <= bufferSize; i++) {
			x[i] = buffer[i];
		}
		for (int i = bufferSize + 1; i <= xlen; i++) {
			x[i] = data[i - bufferSize];
		}
		
		final int halfframe = frame / 2;
		
		if (xlen < 6 * halfframe) {	// 不足5帧（即小于6个半帧的长度）
			bufferSize = xlen;
			for (int i = 1; i <= xlen; i++) {
				buffer[i] = x[i];
			}
			return ;
		}
		
		int nhf = xlen / halfframe;
		bufferSize = xlen - (nhf-5) * halfframe;
		for (int i = (nhf-5) * halfframe + 1; i <= xlen; i++) {
			buffer[i] = x[i];
		}
		
		double[][] ans = mfcc(x, xlen);
		if (ans == null) 
			return ;
		
		try {
			fos = new FileOutputStream(file, true);
		} catch (FileNotFoundException e) {
			return ;
		}
		
		byte[] serial = new byte[4];
		for (int i = 1; i <= mfccNum; i++) {
			for (int j = 1; j <= dim; j++) {
				float tmp = (float) ans[i][j];
				int y = Float.floatToIntBits(tmp);
				for (int k = 0; k < 4; k++)
				{
					serial[k] = (byte) (y & 255);
					y = y >> 8;
				}
				try {
					fos.write(serial);
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
	 * @param x subscript start from 1
	 * @param xlen
	 * @return
	 */
	public double[][] mfcc(double[] x, int xlen) {
		if (x == null)
			return null;

		// xx = double(x);
		// xx = filter([1 -0.9375], 1, xx);
		double[] xx = new double[xlen+1];
		x[0] = 0;
		for (int i = 1; i <= xlen; i++) {
			xx[i] = 1 * x[i] - 0.9375 * x[i-1];
		}
		
		double[][] f = enframe(xx, frame/2);
		int framenum = f.length - 1;
		
		double[] vector = new double[frame+1];
		double[] tmp = new double[p+1];
		double[][] m = new double[framenum+1][dim/2+1];
		double[][] dtm = new double[framenum+1][dim/2+1];
		double[][] c2 = new double[framenum][dim+1];
		for (int i = 1; i <= framenum; i++) {
			
			for (int j = 1; j <= frame; j++)
				vector[j-1] = f[i][j];
			vector = fft(vector, frame);
			for (int j = frame; j >= 1; j--)
				vector[j] = vector[j-1];
			
			for (int j = 1; j <= bankH; j++) {
				tmp[j] = 0;
				for (int k = 1; k <= bankW; k++) {
					tmp[j] += bank[j][k] * vector[k];
				}
			}
			
			for (int j = 1; j <= dim/2; j++) {
				c1[j] = 0;
				for (int k = 1; k <= bankH; k++) {
					c1[j] += dct[j][k] * Math.log(tmp[k]);
				}
			}
			
			for (int j = 1; j <= dim/2; j++) {
				m[i][j] = c1[j] * w[j];
			}
			
		}
		
		memset(dtm, 0);
		for (int i = 3; i <= framenum - 2; i++) {
			// dtm(i,:) = -2*m(i-2,:)-m(i-1,:)+m(i+1,:)+2*m(i+2,:);
			// dtm = dtm/3;
			for (int j = 1; j <= dim/2; j++) {
				dtm[i][j] = (-2 * m[i-2][j] - m[i-1][j] + m[i+1][j] + 2 * m[i+2][j]) / 3;
			}
		}
		
		for (int i = 3; i <= framenum - 2; i++) {
			for (int j = 1; j <= dim/2; j++) {
				c2[i-2][j] = m[i][j];
			}
			for (int j = dim/2+1; j <= dim; j++) {
				c2[i-2][j] = dtm[i][j-dim/2];
			}
		}
		
		mfccNum = framenum - 4;
		
		return c2;
	}
	
	public int getMfccNum() {
		return mfccNum;
	}
	
	/*
	 * 快速傅里叶变换
	 */
	private double[] fft(double[] vector, int framelen)
	{   
	    //输出为dataR存放实数，dataI存放虚数，dataM存放功率谱   
	    double[] dataR = new double[framelen+1];
	    double[] dataI = new double[framelen+1];   
	    //x[N]即为加窗后的语音帧数据      
	    int  x0,x1,x2,x3,x4,x5,x6,x7,xx;   
	    int  L,j,k,b,p,i;   
	    double[] sin_tab = new double[framelen+1];
	    double[] cos_tab = new double[framelen+1];
	    double  TR,TI,temp;   
	       
	    for(i = 0; i < framelen; i++)   
	    {      
	        sin_tab[i] = Math.sin(2 * Math.PI * i /framelen);   
	        cos_tab[i]= Math.cos(2 * Math.PI * i / framelen);                 
	        dataR[i] = vector[i];   
	    }   
	   
	    for(i = 0;i < framelen; i++)     
	    {      
	        x0 = x1 = x2 = x3 = x4 = x5 = x6 = x7 = 0;     
	        x0 = i&0x01;  x1 = (i/2)&0x01;  x2 = (i/4)&0x01;  x3 = (i/8)&0x01;
	        x4 = (i/16)&0x01;  x5 = (i/32)&0x01;  x6 = (i/64)&0x01;  x7 = (i/128)&0x01;   
	        xx = x0 * 128 + x1 * 64 + x2 * 32 + x3 * 16 + x4 * 8 + x5 * 4 + x6 * 2 + x7;     
	        dataI[xx] = dataR[i];
	    }
	    
	    for(i = 0; i < framelen; i++)     
	    {     
	        dataR[i] = dataI[i];     
	        dataI[i] = 0;     
	    }     
	    // FFT
	    for(L = 1; L <= 8; L++)    //  for(1)     
	    {      
	        b = 1;  i = L-1;     
	        while(i > 0)    //  b=  2^(L-1)     
	        {      
	            b = b * 2;     
	            i--;   
	        }   
	        for(j = 0; j <= b-1; j++)  //  for  (2)      
	        {      
	            p = 1; 
	            i = 8 - L;     
	            while(i > 0)  // p=pow(2,7-L)*j;       
	            {   
	                p = p * 2;     
	                i--;   
	            }     
	            p = p * j;     
	            for(k = j; k < framelen; k = k + 2 * b)  //  for  (3)     
	            {      
	                TR = dataR[k];
	                TI = dataI[k];    
	                temp = dataR[k + b];     
	                dataR[k] = dataR[k] + dataR[k + b] * cos_tab[p] + dataI[k + b] * sin_tab[p];     
	                dataI[k] = dataI[k] - dataR[k + b] * sin_tab[p] + dataI[k + b] * cos_tab[p];     
	                dataR[k + b] = TR - dataR[k + b] * cos_tab[p] - dataI[k + b] * sin_tab[p];     
	                dataI[k + b] = TI + temp * sin_tab[p] - dataI[k + b] * cos_tab[p];     
	            }  //  END  for  (3)     
	        }      
	        
	    }  //  END  for  (1)     
	        
	   
	    for(i = 0; i < framelen; i++)   
	    {      
	        vector[i] = dataR[i] * dataR[i] + dataI[i] * dataI[i];    
	    }
	    return vector;
	}

	
	static public double[][] enframe(double[] x, int inc ) {
		int nx = x.length - 1;
		int nwin = hammingwin.length - 1;
		int lw = nwin;
		int nli = nx - lw + inc;
		int nf = nli / inc;
		
		double[][] f = new double[nf+1][lw+1];
		f = memset(f, 0);
		
		for (int i = 1; i <= nf; i++) {
			for (int j = 1; j <= nwin; j++) {
				f[i][j] = x[inc * (i-1) + j] * hammingwin[j];
			}
		}
		
		return f;
	}
	
	static public double[] hamming(int n) {
		double[] win = new double[n+1];
		for (int i = 0; i < n; i++) {
			win[i+1] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / ( n - 1 ));
		}
		return win;
	}
	
	static public double[][] memset(double[][] x, double val) {
		if (x == null)
			return x;
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				x[i][j] = val;
			}
		}
		return x;
	}
	
	
}
