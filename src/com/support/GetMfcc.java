package com.support;

public class GetMfcc {

	private double fs = 44100;
	private int bankL = 0;
	private int bankW = 0;	//bank矩阵bankW * bankL
	private int framenum;
	private int framelen = 256;
	private double[] buffer = new double[framelen * 3];
	private int bufferSize = 0;
	private double[] data;
	private int dim = 26;
	private int p = 24;

	public GetMfcc()
	{
		
	}
	
	public GetMfcc(int dim)
	{
		this.dim = dim;
	}
	
	public GetMfcc(double fs, int dim)
	{
		this.fs = fs;
		this.dim = dim;
	}
	
	public GetMfcc(double fs, int dim, int p)
	{
		this.dim = dim;
		this.fs = fs;
		this.p = p;
	}

	
	public double frq2mel(double frq)
	{
		double k = 1127.01048;
		double af = Math.abs(frq);
		double mel = Math.signum(frq) * Math.log(1 + af / 700) * k;
		return mel;
	}

	public double mel2frq(double mel)
	{
		double k = 1127.01048;
		double frq = 700 * Math.signum(mel) * (Math.exp(Math.abs(mel) / k) - 1);
		return frq;
	}
	
	/*
	 *  p   number of filters in filterbank or the filter spacing in k-mel/bark/erb [ceil(4.6*log10(fs))]
		n   length of fft
		fs  sample rate in Hz
	 */
	
	public double[][] melbank(int p, int n)
	{
		final int N = n;
		double[] mflh = new double[3];
		mflh[1] = frq2mel(0.0);
		mflh[2] = frq2mel(0.5 * fs);
		
		double melrng = -mflh[1] + mflh[2];
		int fn2 = (int) Math.floor(n / 2);
		double melinc = melrng / (p+1);
		
		double[] blim = new double[5];
		blim[1] = 0; 
		blim[2] = 1;
		blim[3] = p;
		blim[4] = p+1;
		
		for(int i = 1; i <= 4; i++)
			blim[i] = mel2frq(mflh[1] + blim[i] * melinc) * n / fs;
		
		int b1 = (int)Math.floor(blim[1]) + 1;
		int b4 = Math.min(fn2, (int)Math.ceil(blim[4]) - 1);
		
		double[] pf = new double[N];
		for(int i = b1; i <= b4; i++)
			pf[i - b1 + 1] = (frq2mel(i * fs / n) - mflh[1]) / melinc;
		
		if(pf[1] < 0)
		{
			for(int i = b1; i < b4; i++)
				pf[i - b1 + 1] = pf[i - b1 + 2];
			b1++;
		}
		
		if(pf[b4 - b1 + 1] >= p + 1)
			b4--;
		
		int[] fp = new int[N];
		for(int i = b1; i <= b4; i++)
			fp[i - b1 + 1] = (int) Math.floor(pf[i - b1 + 1]);
		
		double[] pm = new double[N];
		for(int i = b1; i <= b4; i++)
			pm[i - b1 + 1] = pf[i - b1 + 1] - fp[i - b1 + 1];
		
		int k2 = -1, k3 = -1, k4 = b4 - b1 + 1;
		for(int i = b1; i <= b4; i++)
			if(fp[i - b1 + 1] > 0)
			{
				k2 = i - b1 + 1;
				break;
			}
		for(int i = b4; i >= b1; i--)
			if(fp[i - b1 + 1] < p)
			{
				k3 = i - b1 + 1;
				break;
			}
		if(k2 == -1)
			k2 = k4 + 1;
		if(k3 == -1)
			k3 = 0;
		
		int[] r = new int[N];
		int cnt = 0;
		for(int i = 1; i <= k3; i++)
			r[++cnt] = 1 + fp[i];
		for(int i = k2; i <= k4; i++)
			r[++cnt] = fp[i];

		int[] c = new int[N];
		cnt = 0;
		for(int i = 1; i <= k3; i++)
			c[++cnt] = i;
		for(int i = k2; i <= k4; i++)
			c[++cnt] = i;
		
		double[] v = new double[N];
		cnt = 0;
		for(int i = 1; i <= k3; i++)
			v[++cnt] = pm[i];
		for(int i = k2; i <= k4; i++)
			v[++cnt] = 1 - pm[i];
		int mn = b1 + 1;

		if(b1 < 0)
			for(int i = 1; i <= cnt; i++)
				c[i] = Math.abs(c[i] + b1 - 1) - b1 + 1;

		for(int i = 1; i <= cnt; i++)
			v[i] = 0.5 - 0.46 / 1.08 * Math.cos(v[i] * Math.PI);

		for(int i = 1; i <= cnt; i++)
		{
			if(c[i] + mn > 2 && (c[i] + mn < n - fn2 + 2))
				v[i] = 2 * v[i];
		}

		double[][] bank = new double[p+1][N];
		
		for(int i = 1; i <= p; i++)
			for(int j = 1; j <= 1 + fn2; j++)
				bank[i][j] = 0;
		
		double Max = -1e30;
		for(int i = 1; i <= cnt; i++)
			if(v[i] > Max)
				Max = v[i];
		for(int i = 1; i <= cnt; i++)
			bank[r[i]][c[i] + mn - 1] = v[i] / Max;
		
		bankL = fn2 + 1;
		bankW = p;		
		return bank;
	}
	
	/*
	 * 汉明窗
	 */
	public double[] hamming(int framelen)
	{
		double[] win = new double[framelen + 1];
		for(int i = 0; i < framelen; i++)
			win[i+1] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (framelen - 1));
		return win;
	}
	
	/*
	 * 分帧，加hamming窗
	 */
	public double[][] enframe(double[] data, int framelen)
	{
		int datalen = data.length;
		int inc = framelen / 2;
		int nli = datalen - framelen / 2;
		framenum = (int)(nli / inc);
		double[][] f = new double[framenum+1][framelen + 1];
		
		for(int i = 1; i <= framenum ; i++)
			for(int j = 1; j <= framelen; j++)
				f[i][j] = 0;

		for(int i = 1; i <= framenum ; i++)
			for(int j = 1; j <= framelen; j++)
				f[i][j] = data[inc * (i-1) + j - 1];
		
		if(framelen > 1)
		{
			double[] win = hamming(framelen);
			for(int i = 1; i <= framenum ; i++)
				for(int j = 1; j <= framelen; j++)
					f[i][j] = f[i][j] * win[j];
		}
		return f;
	}
	
	/*
	 * 快速傅里叶变换
	 */
	public double[] fft(double[] vector, int framelen)
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
	
	/*
	 * p滤波器数目
	 * dim为要提取特征的维数
	 * 下标从1开始
	 */
	
	public double[][] mfcc(double[] idata, int ilen)
	{
		/*
		 * 缓冲 前2.5*framelen个数据
		 */
		int len;
		if(bufferSize >= (int)(2.5 * framelen))
			len = (int)(2.5 * framelen);
		else
			len = bufferSize;
		
		data = new double[ilen + len];
		for(int i = 0; i < len; i++)
			data[i] = buffer[i];
			
		for(int i = len; i < data.length; i++)
			data[i] = idata[i - len];

		if(data.length >= (int)(2.5 * framelen))
		{
			bufferSize = (int)(2.5 * framelen);
			for(int i = data.length - bufferSize; i < data.length; i++)
				buffer[i - data.length + bufferSize] = data[i];
		}
		else
		{
			bufferSize = data.length;
			for(int i = 0; i < bufferSize; i++)
				buffer[i] = data[i];
		}
		
		double[][] bank = melbank(p, framelen);
		double[][] dct = new double[dim/2+1][p+1];
		double[] w= new double[dim/2+1]; 
		
		for(int i = 1; i <= dim/2; i++)
			for(int j = 0; j < p; j++)
				dct[i][j + 1] = Math.cos((2 * j + 1) * i * Math.PI / 2 / p);
		
		double Max = -1e30;
		for(int i = 1; i <= dim/2; i++)
		{
			w[i] = 1 + 6 * Math.sin(Math.PI * i / 16);
			Max = Math.max(Max, w[i]);
		}
		
		for(int i = 1; i <= dim/2; i++)
			w[i] = w[i] / Max;
		
		for(int i = data.length - 1; i >= 1; i--)
			data[i] = data[i] - 0.9375 * data[i-1];
		
		double[][] f = enframe(data, framelen);
		
		if(framenum < 4)
			return null;
		
		double[][] m = new double[framenum+1][dim/2+1];
		double[][] dtm = new double[framenum+1][dim/2+1];
		double[][] mfcc = new double[framenum][dim+1];
		double[] vector = new double[framelen+1];
		double[] tmp = new double[p+1];
		
		for(int i =1; i <= framenum; i++)
		{
			for(int j = 1; j <=framelen; j++)
				vector[j - 1] = f[i][j];
			
			vector = fft(vector, framelen);
			
			for(int j = framelen; j >= 1; j--)
				vector[j] = vector[j - 1];
			
			for(int j = 1; j <= p; j++)
				tmp[j] = 0;
			
			for(int j = 1; j <= p; j++)
			{
				for(int k = 1; k <= bankL; k++)
					tmp[j] += vector[k] * bank[j][k];
				tmp[j] = Math.log(tmp[j]);
			}
			
			double[] c1 = new double[dim/2+1];
			for(int j = 1; j <= dim/2; j++)
				c1[j] = 0;
			
			for(int j = 1; j <= dim/2; j++)
				for(int k = 1; k <= p; k++)
					c1[j] += dct[j][k] * tmp[k];
			
			for(int j = 1; j <= dim/2; j++)
				m[i][j] = c1[j] * w[j];
		}
		
		for(int i = 1; i <= framenum; i++)
			for(int j = 1; j <= dim/2; j++)
				dtm[i][j] = 0;
		
		for(int i = 3; i <= framenum-2; i++)
			for(int j = 1; j<=dim/2; j++)
				dtm[i][j] = (-2 * m[i-2][j] - m[i-1][j] + m[i+1][j] + 2 * m[i+2][j]) / 3;
		
		for(int i = 3; i <= framenum-2; i++)
		{
			for(int j = 1; j <= dim/2; j++)
				mfcc[i-2][j] = m[i][j];
			for(int j = dim/2+1; j <= dim; j++)
				mfcc[i-2][j] = dtm[i][j-dim/2];
		}
		return mfcc;
	}	
	
	public double[][] mfcc(double[] idata)
	{
		return mfcc(idata, idata.length);
	}
	
	public int getFramenum()
	{
		if(framenum >= 4)
			return framenum - 4;
		else
			return 0;
	}
	
	public int getFramelen()
	{
		return framelen;
	}
	
	public int getDimension()
	{
		return dim;
	}
}
