package com.support.mfcc;

public class Mfcc {

	static int dim = 26;
	static int p = 24;
	static int fs = 8000;
	static int frame = 256;
	
	static double[][] dct = new double[dim/2+1][p+1];
	static double[] w = new double[dim/2+1];
	static double[][] bank = MelBankm.melbankm(p, frame, fs, 0, 0.5);
	static double[] hammingwin = hamming(frame);
	
	static public double[] mfcc(double[] x, int xlen) {
		if (x == null)
			return null;
		
		// bank = melbankm(24, 256, fs, 0, 0.5, 'm');
		// bank = full(bank);
		// bank = bank/max(bank(:));
		int height = bank.length - 1;
		int width = bank[0].length - 1;
		
		double bankMax = -1e30;
		for (int i = 1; i <= height; i++) {
			for (int j = 1; j <= width; j++) {
				bankMax = Math.max(bankMax, bank[i][j]);
			}
		}
		for (int i = 1; i <= height; i++) {
			for (int j = 1; j <= width; j++) {
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
		
		// xx = double(x);
		// xx = filter([1 -0.9375], 1, xx);
		double[] xx = new double[xlen+1];
		x[0] = 0;
		for (int i = 1; i <= xlen; i++) {
			xx[i] = 1 * x[i] - 0.9375 * x[i-1];
		}
		
		double[][] f = enframe(xx, frame/2);
		
		return null;
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
				f[i][j] = x[inc * (i-1) + j - 1] * hammingwin[j];
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
