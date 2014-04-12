#include <jni.h>
#include "com_support_Recognition.h"

#include <math.h>
#include <malloc.h>
#include <iostream>
#include <fstream>

#include "alize.h"
#include "string.h"
#include "Config.h"

#pragma comment(lib, "libalize_Linux_i686.a")
#define pi 3.1415926
#define MAX_N 1000005
#define MAX_FILTER 35
#define MAX_VEC 35
#define MAX_FRM 1024

#define MIN_COV 1e-200
#define N 256

using namespace alize;

double bank[MAX_FILTER+1][MAX_FRM+1];
double dct[MAX_VEC/2+1][MAX_FILTER+1];
double f[2*MAX_N/MAX_FRM+1];
double w[MAX_VEC/2+1],win[MAX_FRM+1],vector[MAX_FRM+1];
double tmp[MAX_FILTER+1], c1[MAX_VEC/2+1], x[MAX_N], re[MAX_N];

void mfcc(double x[], double re[]);

void trainGMM(Config config, char* worldPath, char* featurePath, char* modelPath, char* filename);
void processMAP(const Mixture& world, Mixture& client, MixtureStat& clientStat);
double test(Config config, char* testFeaturePath, char* worldPath, char* modelPath, char* filename);
double c_recognition(char* rootPath, char* filename);

/*
 * Class:     com_support_Recognition
 * Method:    jniTrainGmm
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_support_Recognition_jniTrainGmm(JNIEnv * env, jclass j, jstring rootPath, jstring filename)
{
	char* path = (char*)env->GetStringUTFChars(rootPath, 0);
	char* name = (char*)env->GetStringUTFChars(filename, 0);
	
	/*
		配置目录
	*/
	char featurePath[N], modelPath[N], worldPath[N], testFeaturePath[N];
//	strcpy(rootPath, ".");
	sprintf(featurePath, "%s/feature/", path);
	sprintf(worldPath, "%s/world/", path);
	sprintf(modelPath, "%s/model/", path);
	sprintf(testFeaturePath, "%s/testFeature/", path);

	/*
		配置向量大小,分布敿文件吿
	*/
	char vectSize[10], distribCount[10];
	strcpy(vectSize, "26");
	strcpy(distribCount, "1024");
//	strcpy(filename, "1");

	Config config;
	/*
		配置FeatureServer
	*/
	config.setParam("loadFeatureFileFormat", "RAW");
	config.setParam("loadFeatureFileExtension", ".mfcc");
	config.setParam("loadFeatureFileVectSize", vectSize);
	config.setParam("featureServerMemAlloc", "100000");

	/*
		配置MixtureServer StatServer
	*/
	config.setParam("vectSize",vectSize);
	config.setParam("mixtureDistribCount", distribCount);
	config.setParam("topDistribsCount", distribCount);
	config.setParam("computeLLKWithTopDistribs", "PARTIAL");
	config.setParam("loadMixtureFileFormat", "RAW");
	config.setParam("loadMixtureFileExtension", ".mdl");
	config.setParam("saveMixtureFileFormat", "RAW");
	config.setParam("saveMixtureFileExtension", ".mdl");
	config.setParam("minLLK", "-100");
	config.setParam("maxLLK", "100");
	config.setParam("distribType", "GD");
	
	trainGMM(config, worldPath, featurePath, modelPath, name);
}

/*
 * Class:     com_support_Recognition
 * Method:    jniTest
 * Signature: (Ljava/lang/String;Ljava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_com_support_Recognition_jniTest(JNIEnv * env, jclass j, jstring rootPath, jstring filename)
{
	char* path = (char*)env->GetStringUTFChars(rootPath, 0);
	char* name = (char*)env->GetStringUTFChars(filename, 0);
	
	/*
		配置目录
	*/
	char featurePath[N], modelPath[N], worldPath[N], testFeaturePath[N];
//	strcpy(rootPath, ".");
	sprintf(featurePath, "%s/feature/", path);
	sprintf(worldPath, "%s/world/", path);
	sprintf(modelPath, "%s/model/", path);
	sprintf(testFeaturePath, "%s/testFeature/", path);

	/*
		配置向量大小,分布敿文件吿
	*/
	char vectSize[10], distribCount[10];
	strcpy(vectSize, "26");
	strcpy(distribCount, "1024");
//	strcpy(filename, "1");

	Config config;
	/*
		配置FeatureServer
	*/
	config.setParam("loadFeatureFileFormat", "RAW");
	config.setParam("loadFeatureFileExtension", ".mfcc");
	config.setParam("loadFeatureFileVectSize", vectSize);
	config.setParam("featureServerMemAlloc", "100000");

	/*
		配置MixtureServer StatServer
	*/
	config.setParam("vectSize",vectSize);
	config.setParam("mixtureDistribCount", distribCount);
	config.setParam("topDistribsCount", distribCount);
	config.setParam("computeLLKWithTopDistribs", "PARTIAL");
	config.setParam("loadMixtureFileFormat", "RAW");
	config.setParam("loadMixtureFileExtension", ".mdl");
	config.setParam("saveMixtureFileFormat", "RAW");
	config.setParam("saveMixtureFileExtension", ".mdl");
	config.setParam("minLLK", "-100");
	config.setParam("maxLLK", "100");
	config.setParam("distribType", "GD");
	
	double score = test(config, testFeaturePath, worldPath, modelPath, name);
	return score;

}

double c_recognition(char* rootPath, char* filename)
{
	/*
		配置目录
	*/
	char featurePath[N], modelPath[N], worldPath[N], testFeaturePath[N];
//	strcpy(rootPath, ".");
	sprintf(featurePath, "%s/feature/", rootPath);
	sprintf(worldPath, "%s/world/", rootPath);
	sprintf(modelPath, "%s/model/", rootPath);
	sprintf(testFeaturePath, "%s/testFeature/", rootPath);

	/*
		配置向量大小,分布�?文件�?
	*/
	char vectSize[10], distribCount[10];
	strcpy(vectSize, "26");
	strcpy(distribCount, "1024");
//	strcpy(filename, "1");


	Config config;
	/*
		配置FeatureServer
	*/
	config.setParam("loadFeatureFileFormat", "RAW");
	config.setParam("loadFeatureFileExtension", ".mfcc");
	config.setParam("loadFeatureFileVectSize", vectSize);
	config.setParam("featureServerMemAlloc", "100000");

	/*
		配置MixtureServer StatServer
	*/
	config.setParam("vectSize",vectSize);
	config.setParam("mixtureDistribCount", distribCount);
	config.setParam("topDistribsCount", distribCount);
	config.setParam("computeLLKWithTopDistribs", "PARTIAL");
	config.setParam("loadMixtureFileFormat", "RAW");
	config.setParam("loadMixtureFileExtension", ".mdl");
	config.setParam("saveMixtureFileFormat", "RAW");
	config.setParam("saveMixtureFileExtension", ".mdl");
	config.setParam("minLLK", "-100");
	config.setParam("maxLLK", "100");
	config.setParam("distribType", "GD");


	trainGMM(config, worldPath, featurePath, modelPath, filename);
	double score = test(config, testFeaturePath, worldPath, modelPath, filename);
	return score;
	//return 0;
}

void trainGMM(Config config, char* worldPath, char* featurePath, char* modelPath, char* filename)
{
	config.setParam("mixtureFilesPath", worldPath);
	MixtureServer ms(config);
	StatServer ss(config, ms);
	
	MixtureGD& world = ms.loadMixtureGD("WorldModel");
	config.setParam("mixtureFilesPath", modelPath);
	config.setParam("featureFilesPath", featurePath);
	
	Feature f;
	FeatureServer fs(config, filename);
	MixtureGD& client = ms.duplicateMixture(world);
	MixtureStat& clientStat = ss.createAndStoreMixtureStat(client);
	
	for(int it = 0; it < 10; it++)
	{
//		cout<<it<<"  "<<endl;
		ss.resetEM(client);
		fs.reset();
		// TODO start
		while(fs.readFeature(f))
			if(f.isValid())
				ss.computeAndAccumulateEM(client, f);
		client = ss.getEM(client);
		// TODO end
		processMAP(world, client, clientStat);
		ss.resetLLK(client);
		fs.reset();
		while(fs.readFeature(f))
			if(f.isValid())
				ss.computeAndAccumulateLLK(client, f);
	}
	client.save(filename, config);
}

void processMAP(const Mixture& world, Mixture& client, MixtureStat& clientStat)
{
	double rhoW = 16;
	double rhoM = 16;
	double rhoV = 16;
	double alphaW = 0.0;
	double alphaM = 0.0;
	double alphaV = 0.0;

	unsigned long c;
	unsigned long dDistriCount = client.getDistribCount();
	unsigned long vectSize = client.getVectSize();
	if (dDistriCount != world.getDistribCount())
	{
		printf("the distribution counts of the models must be the same!" );
		return;
	}
	MixtureGDStat& clientGDStat = static_cast<MixtureGDStat&>(clientStat);
	DoubleVector& clientAccumuOccVect = clientStat.getAccumulatedOccVect();

	double totOcc = 0.0;
	for(c = 0; c < dDistriCount; c++)
		totOcc += clientAccumuOccVect[c];

	for(c = 0; c < dDistriCount; c++)
	{
		double occ = clientStat.getAccumulatedOccVect()[c];
		if(occ > 0.0)
		{
			DistribGD& clientDistrib = static_cast<DistribGD&>(client.getDistrib(c));
			double*  dClientCovVect = clientDistrib.getCovVect().getArray();
			double*  dClientMeanVect = clientDistrib.getMeanVect().getArray();

			DistribGD& worldDistrib = static_cast<DistribGD&>(world.getDistrib(c));
			double* dWorldCovVect = worldDistrib.getCovVect().getArray();
			double* dWorldMeanVect = worldDistrib.getMeanVect().getArray();

			DistribGD& clientStatForAccumu = static_cast<DistribGD&>(clientGDStat.getInternalAccumEM().getDistrib(c));
			double* dTmpCovVect = clientStatForAccumu.getCovVect().getArray();
			double* dTmpMeanVect = clientStatForAccumu.getMeanVect().getArray();

			double mean, cov;

			alphaW = occ / (occ + rhoW);
			alphaM = occ / (occ + rhoM);
			alphaV = occ / (occ + rhoV);

			for(unsigned long i = 0; i < vectSize; i++)
			{
				mean = alphaM * dTmpMeanVect[i] / occ + (1 - alphaM) * dWorldMeanVect[i];
				cov = alphaV * dTmpCovVect[i] / occ - mean * mean + (1 - alphaV) * (dWorldCovVect[i] + dWorldMeanVect[i] * dWorldMeanVect[i]);
				dClientMeanVect[i] = mean;
				if(cov > 0.0)
					dClientCovVect[i] = cov;
				else
					dClientCovVect[i] = MIN_COV;
			}
			clientDistrib.computeAll();
			client.getTabWeight()[c] = alphaW * occ / totOcc + (1 - alphaW) * world.getTabWeight()[c];
		}

	}
	double newTotOcc = 0.0;
	for(c = 0; c < dDistriCount; c++)
		newTotOcc += client.getTabWeight()[c];
	for(c = 0; c < dDistriCount; c++)
		client.getTabWeight()[c] = client.getTabWeight()[c] / newTotOcc;
}

double test(Config config, char* testFeaturePath, char* worldPath, char* modelPath, char* filename)
{
	double worldScore = 0;
	double clientScore = 0;

	MixtureServer ms(config);
	StatServer ss(config, ms);
	config.setParam("mixtureFilesPath", worldPath);
	MixtureGD& world = ms.loadMixtureGD("WorldModel");
	config.setParam("mixtureFilesPath", modelPath);
	config.setParam("featureFilesPath", testFeaturePath);
	Feature f;
	ms.loadMixtureGD(filename);
	
	int distribCount = ms.getMixtureCount();
	FeatureServer fs(config, filename);
	
	for(int dc = 0; dc < distribCount; dc++)
	{
		MixtureGD& curMix = (MixtureGD&)ms.getMixture(dc);
		ss.resetLLK(curMix);
		fs.reset();
		while(fs.readFeature(f))
			if(f.isValid())
				ss.computeAndAccumulateLLK(curMix, f);
		if(dc == 0)
			worldScore = ss.getMeanLLK(curMix);
		else
		{
			clientScore = ss.getMeanLLK(curMix);
			return clientScore - worldScore;
		}
	}
	return -10000000;
}

int sign(double a)
{
	if(a < 0)	return -1;
	if(a > 0)	return 1;
	if(a == 0)	return 0;
}

double frq2mel(double frq)
{
	double k = 1127.01048;
	double af = fabs(frq);
	double mel = sign(frq)*log(1+af/700)*k;
	return mel;
}

double mel2frq(double mel)
{
	double k = 1127.01048;
	double frq = 700*sign(mel)*(exp(fabs(mel)/k)-1);
	return frq;
}
int min(int a, int b)
{
	if(a < b)	return a;
	else		return b;
}

void melbankm(int p, int n, double fs, double fl, double fh, double bank[][MAX_FRM+1], int& ll)
{
	double mflh[3];
	int cnt = 0, r[MAX_FRM+1], c[MAX_FRM+1];
	double v[MAX_FRM+1],pf[MAX_FRM+1],pm[MAX_FRM+1];

	mflh[1] = fl*fs; mflh[2] = fh*fs;
	mflh[1] = frq2mel(mflh[1]);
	mflh[2] = frq2mel(mflh[2]);

	double melrng = -mflh[1]+mflh[2];
	int fn2 = floor((double)n/2);

	double melinc = melrng/(p+1);
	double blim[5];
	blim[1] = 0; blim[2] = 1; blim[3] = p;  blim[4] = p+1;

	for(int i=1; i<=4; i++)
		blim[i] = mel2frq(mflh[1]+blim[i]*melinc)*n/fs;

	int b1 = floor(blim[1])+1;
	int b4 = min(fn2, ceil(blim[4])-1);

	for(int i=b1; i<=b4; i++)
		pf[i-b1+1] = (frq2mel(i*fs/n)-mflh[1])/melinc;
	if(pf[1] < 0)
	{
		for(int i=b1; i<b4; i++)
			pf[i-b1+1] = pf[i-b1+2];
		b1 = b1+1;
	}
	if(pf[b4-b1+1] >= p+1)
		b4 = b4-1;

	int fp[300];
	for(int i=b1; i<=b4; i++)
		fp[i-b1+1] = floor(pf[i-b1+1]);

	for(int i=b1; i<=b4; i++)
		pm[i-b1+1] = pf[i-b1+1]-fp[i-b1+1];

	int k2 = -1, k3 = -1, k4 = b4-b1+1;
	for(int i=b1; i<=b4; i++)
		if(fp[i-b1+1] > 0)
		{
			k2 = i-b1+1;
			break;
		}
	for(int i=b4; i>=b1; i--)
		if(fp[i-b1+1] < p)
		{
			k3 = i-b1+1;
			break;
		}
	if(k2 == -1)
		k2 = k4+1;
	if(k3 == -1)
		k3 = 0;

	for(int i=1; i<=k3; i++)
		r[++cnt] = 1+fp[i];
	for(int i=k2; i<=k4; i++)
		r[++cnt] = fp[i];

	cnt = 0;
	for(int i=1; i<=k3; i++)
		c[++cnt] = i;
	for(int i=k2; i<=k4; i++)
		c[++cnt] = i;

	cnt = 0;
	for(int i=1; i<=k3; i++)
		v[++cnt] = pm[i];
	for(int i=k2; i<=k4; i++)
		v[++cnt] = 1-pm[i];
	int mn = b1+1;

	if(b1 < 0)
		for(int i=1; i<=cnt; i++)
			c[i] = abs(c[i]+b1-1)-b1+1;

	for(int i=1; i<=cnt; i++)
		v[i] = 0.5-0.46/1.08*cos(v[i]*pi);

	int msk;
	for(int i=1; i<=cnt; i++)
	{
		if(c[i]+mn>2&&(c[i]+mn<n-fn2+2))
			v[i] = 2*v[i];
	}

	for(int i=1; i<=p; i++)
		for(int j=1; j<=1+fn2; j++)
			bank[i][j] = 0;
	ll = 1+fn2;
	double Max = -1;
	for(int i=1; i<=cnt; i++)
		if(v[i] > Max)
			Max = v[i];
	for(int i=1; i<=cnt; i++)
		bank[r[i]][c[i]+mn-1] = v[i]/Max;
}



void filter(double a, double x[], int len)  //s(n)-a*s(n-1)
{
	for(int i=len; i>=2; i--)
	{
		x[i] = x[i]-a*x[i-1];
	}
}





int fix(double a)
{
	return (int)a;
}

void hamming(int n, double win[])
{
	for(int i=0; i<n; i++)
		win[i+1] = 0.54-0.46*cos(2*pi*i/(n-1));
}

void enframe(double x[], int nx, double win[], int nwin, double f[])
{
	int lw = nwin;
	for(int j=1; j<=lw; j++)
		f[j] = 0;

	for(int j=1; j<=lw; j++)
		f[j] = x[j];

	if(nwin > 1)
	{
		for(int j=1; j<=lw; j++)
			f[j] = f[j]*win[j];
	}
}



void fft(double vector[],int LenFrame)
{
    int i=0;
    double dataR[MAX_FRM+1],dataI[MAX_FRM+1];
    int  x0,x1,x2,x3,x4,x5,x6,x7,xx;
    int  L,j,k,b,p;
    double  *sin_tab,*cos_tab;
    double  TR,TI,temp;
    sin_tab=(double *)malloc (LenFrame * sizeof (double));
    cos_tab=(double *)malloc (LenFrame * sizeof (double));
    for(i=0;i<LenFrame;i++)
    {
        *(sin_tab+i)=sin(2*pi*i/LenFrame);
        *(cos_tab+i)=cos(2*pi*i/LenFrame);
        dataR[i]=*(vector+i);
    }

    for(i=0;i<LenFrame;i++)
    {
        x0=x1=x2=x3=x4=x5=x6=x7=0;
        x0=i&0x01;  x1=(i/2)&0x01;  x2=(i/4)&0x01;  x3=(i/8)&0x01;x4=(i/16)&0x01;  x5=(i/32)&0x01;  x6=(i/64)&0x01;  x7=(i/128)&0x01;
        xx=x0*128+x1*64+x2*32+x3*16+x4*8+x5*4+x6*2+x7;
        dataI[xx]=dataR[i];
    }
    for(i=0;i<LenFrame;i++)
    {
        dataR[i]=dataI[i];
        dataI[i]=0;
    }
    // FFT
    for(L=1;L<=8;L++)    //  for(1)
    {
        b=1;  i=L-1;
        while(i>0)    //  b=  2^(L-1)
        {
            b=b*2;
            i--;
        }
        for(j=0;j<=b-1;j++)  //  for  (2)
        {
            p=1;
            i=8-L;
            while(i>0)  // p=pow(2,7-L)*j;
            {
                p=p*2;
                i--;
            }
            p=p*j;
            for(k=j;k<LenFrame;k=k+2*b)  //  for  (3)
            {
                TR=dataR[k];
                TI=dataI[k];
                temp=dataR[k+b];
                dataR[k]=dataR[k]+dataR[k+b]*cos_tab[p]+dataI[k+b]*sin_tab[p];
                dataI[k]=dataI[k]-dataR[k+b]*sin_tab[p]+dataI[k+b]*cos_tab[p];
                dataR[k+b]=TR-dataR[k+b]*cos_tab[p]-dataI[k+b]*sin_tab[p];
                dataI[k+b]=TI+temp*sin_tab[p]-dataI[k+b]*cos_tab[p];
            }  //  END  for  (3)
        }

    }  //  END  for  (1)


    for(i=0;i<LenFrame;i++)
    {
        *(vector+i)=dataR[i]*dataR[i]+dataI[i]*dataI[i];
    }
    free(sin_tab);
    free(cos_tab);

}



void mfcc(double x[], double re[])
{
	int ll;
	int framelen = 1024;
	int p = 13;
	int xlen = 1024;
	double fs = 2050;
	int dim = 26;

	melbankm(p, 256, fs, 0, 0.5, bank, ll);
	for(int i=1; i<=dim/2; i++)
		for(int j=0; j<p; j++)
			dct[i][j+1] = cos((2*j+1)*i*pi/(2*p));

	double Max = -1;
	for(int i=1; i<=dim/2; i++)
	{
		w[i] = 1+6*sin(pi*i/(dim/2));
		if(w[i] > Max)
			Max = w[i];
	}
	for(int i=1; i<=dim/2; i++)
		w[i] = w[i]/Max;

	filter(0.9375, x, xlen);
	hamming(framelen, win);

	int  nf;
	enframe(x, xlen, win, framelen, f);

	for(int j=1; j<=framelen; j++)
		vector[j-1] = f[j];

	fft(vector, framelen);
	for(int j=framelen; j>=1; j--)
		vector[j] = vector[j-1];

	for(int j=1; j<=p; j++)
		tmp[j] = 0;
	for(int j=1; j<=p; j++)
	{
		for(int k=1; k<=ll; k++)
			tmp[j] += vector[k]*bank[j][k];
		tmp[j] = log(tmp[j]);
	}

	for(int j=1; j<=dim/2; j++)
		c1[j] = 0;

	for(int j=1; j<=dim/2; j++)
		for(int k=1; k<=p; k++)
			c1[j] += dct[j][k]*tmp[k];

	for(int j=1; j<=dim/2; j++)
	{
		re[j] = c1[j]*w[j];
		re[dim/2+j] = re[j];
	}
}
