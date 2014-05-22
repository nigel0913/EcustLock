package com.support.mfcc;

import android.util.Log;

public class MelBankm {

	static double[][] x = new double[48][256];
	static int kp = 0;
	static int kn = 0;
	static int kfs = 0;
	static double kfl = 0;
	static double kfh = 0;
	
	static public double[][] melbankm(int p, int n, int fs, double fl, double fh) {
		
		if (kp==p && kn==n && kfs==fs && kfl==fl && kfh==fh) {
			return x;
		}
		kp=p;
		kn=n;
		kfs=fs;
		kfl=fl;
		kfh=fh;
		
		double[] mflh = new double[3];
		double melrng = 0;
		double melinc = 0;
		double[] blim = new double[5];

		mflh[1] = fl * fs; mflh[2] = fh * fs;
		for (int i = 1; i <= 2; i++) {
			mflh[i] = frq2mel(mflh[i]);
		}
		
		melrng = -mflh[1] + mflh[2];	// melrng=mflh*(-1:2:1)';          % mel range
		Log.d("mfcc", "melrng="+melrng);
		
		int fn2 = (int) Math.floor( n / 2 );
		melinc = melrng / (p + 1);
		Log.d("mfcc", "melinc="+melinc+",fn2="+fn2);
		
		// blim=mel2frq(mflh(1)+[0 1 p p+1]*melinc)*n/fs;
		double[] tmp = {-1, 0, 1, p, p+1};
		for (int i = 1; i <= 4; i++) {
			blim[i] = mel2frq( mflh[1] + tmp[i]*melinc ) * n / fs;
		}
		
		// b1=floor(blim(1))+1;            % lowest FFT bin_0 required might be negative)
		// b4=min(fn2,ceil(blim(4))-1);    % highest FFT bin_0 required
		int b1 = (int) (Math.floor( blim[1] ) + 1);
		int b4 = (int) Math.min(fn2, Math.ceil(blim[4]) - 1);
		Log.d("mfcc", "b1="+b1+",b4="+b4);
		
		// pf=( frq2mel( (b1:b4) * fs / n )-mflh(1) )/melinc;
		double[] pf = new double[b4 - b1 + 2];
		for (int i = 1; i <= b4 - b1 + 1; i++) {
			pf[i] = ( frq2mel( (b1+i-1) * fs * 1.0 / n ) - mflh[1] ) / melinc;
		}
		
		if (pf[1] < 0) {
			for (int i = 1; i < b4 - b1 + 1; i++) {
				pf[i] = pf[i+1];
			}
			b1 = b1 +1;
		}
		
		if (pf[b4-b1+1] >= p+1) {
			pf[b4-b1+1] = 0;
			b4 = b4 - 1;
		}
		
		int pflen = b4 - b1 + 1;
		Log.d("mfcc", "pflen="+pflen);
		int[] fp = new int[pflen+1];
		double[] pm = new double[pflen+1];
		for (int i = 1; i <= pflen; i++) {
			fp[i] = (int) Math.floor(pf[i]);
		}
		for (int i = 1; i <= pflen; i++) {
			pm[i] = pf[i] - fp[i];
		}
		
		int k2 = -1, k3 = -1, k4 = -1;
		for (int i = 1; i <= pflen; i++) {
			if (fp[i] > 0) {
				k2 = i;
				break;
			}
		}
		for (int i = pflen; i >= 1; i--) {
			if (fp[i] < p) {
				k3 = i;
				break;
			}
		}
		k4 = pflen;		// k4=numel(fp); % FFT bin_1 k4+b1 is the last to contribute to any filters
		if ( k2 == -1 ) {
			k2 = k4 + 1;
		}
		if ( k3 == -1 ) {
			k3 = 0;
		}
		// r=[1+fp(1:k3) fp(k2:k4)]; % filter number_1
		int rlen = k3 + k4 - k2 + 1;
		Log.d("mfcc", "k2="+k2+",k3="+k3+",k4="+k4);
		Log.d("mfcc", "rlen="+rlen);
		int[] c = new int[rlen+1];
		int[] r = new int[rlen+1];
		double[] v = new double[rlen+1];
		
		// c=[1:k3 k2:k4]; % FFT bin_1 - b1
		for (int i = 1; i <= k3; i++) {
			c[i] = i;
		}
		for (int i = k3+1; i <= rlen; i++) {
			c[i] = k2 - 1 + i - k3;
		}
		for (int i = 1; i <= k3; i++) {
			r[i] = 1 + fp[i];
		}
		for (int i = k3+1; i <= rlen; i++) {
			r[i] = fp[ c[i] ];
		}
		// v=[pm(1:k3) 1-pm(k2:k4)];
		for (int i = 1; i <= k3; i++) {
			v[i] = pm[i];
		}
		for (int i = k3+1; i <= rlen; i++) {
			v[i] = 1 - pm[ c[i] ];
		}
		
		int mn = b1 + 1;
		int mx = b4 + 1;
		
		if (b1 < 0) {
			for (int i = 1; i <= rlen; i++) {
				c[i] = Math.abs(c[i] + b1 - 1) - b1 + 1;
			}
		}
		for (int i = 1; i <= rlen; i++) {
			v[i] = 0.5 - 0.46 / 1.08 * Math.cos(v[i] * Math.PI);
		}
		
		// msk=(c+mn>2) & (c+mn<n-fn2+2);  % there is no Nyquist term if n is odd
	    // v(msk)=2*v(msk);
		for (int i = 1; i <= rlen; i++) {
			if ( c[i] + mn > 2 && c[i] + mn < n - fn2 + 2 ) {
				v[i] = 2 * v[i];
			}
		}
		
		for (int i = 1; i <= p; i++) {
			for (int j = 1; j <= 1+fn2; j++) {
				x[i][j] = 0;
			}
		}
		for (int i = 1; i <= rlen; i++) {
			x[ r[i] ][ c[i] + mn - 1 ] = v[i];
		}
		
		return x;
	}
	
	
	/**
	 * @param frq
	 * @return mel = sign(frq).*log(1+af/700)*k;
	 */
	static public double frq2mel(double frq) {
		double k = 1127.01048;
		double af = Math.abs(frq);
		return Math.signum(frq) * Math.log(1 + af / 700) * k;
	}
	
	/**
	 * @param mel
	 * @return frq=700*sign(mel).*(exp(abs(mel)/k)-1);
	 */
	static public double mel2frq(double mel) {
		double k = 1127.01048;
		return 700 * Math.signum(mel) * (Math.exp( Math.abs(mel) / k ) - 1);
	}
}
