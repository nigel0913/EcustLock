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

void trainGMM(Config config, char* worldPath, char* featurePath, char* modelPath, char* filename);
void processMAP(const Mixture& world, Mixture& client, MixtureStat& clientStat);
double test(Config config, char* worldPath, char* testFeaturePath, char* modelPath, char* filename);

/*
 * Class:     com_support_Recognition
 * Method:    jniTrainGmm
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_support_Recognition_jniTrainGmm(JNIEnv * env, jclass j, jstring jWorldPath, jstring jFeaPath, jstring jMdlPath, jstring jFilename)
{
    char* worldPath = (char*) env->GetStringUTFChars(jWorldPath, 0);
    char* feaPath = (char*) env->GetStringUTFChars(jFeaPath, 0);
    char* mdlPath = (char*) env->GetStringUTFChars(jMdlPath, 0);
    char* filename = (char*) env->GetStringUTFChars(jFilename, 0);


	char vectSize[10], distribCount[10];
	strcpy(vectSize, "26");
	strcpy(distribCount, "1024");

	Config config;

	config.setParam("loadFeatureFileFormat", "RAW");
	config.setParam("loadFeatureFileExtension", ".mfcc");
	config.setParam("loadFeatureFileVectSize", vectSize);
	config.setParam("featureServerMemAlloc", "100000");

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

	trainGMM(config, worldPath, feaPath, mdlPath, filename);
}

/*
 * Class:     com_support_Recognition
 * Method:    jniTest
 * Signature: (Ljava/lang/String;Ljava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_com_support_Recognition_jniTest(JNIEnv * env, jclass j, jstring jWorldPath, jstring jFeaPath, jstring jMdlPath, jstring jFilename)
{
    char* worldPath = (char*) env->GetStringUTFChars(jWorldPath, 0);
    char* feaPath = (char*) env->GetStringUTFChars(jFeaPath, 0);
    char* mdlPath = (char*) env->GetStringUTFChars(jMdlPath, 0);
    char* filename = (char*) env->GetStringUTFChars(jFilename, 0);

	char vectSize[10], distribCount[10];
	strcpy(vectSize, "26");
	strcpy(distribCount, "1024");

	Config config;

	config.setParam("loadFeatureFileFormat", "RAW");
	config.setParam("loadFeatureFileExtension", ".mfcc");
	config.setParam("loadFeatureFileVectSize", vectSize);
	config.setParam("featureServerMemAlloc", "100000");


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

	return test(config, feaPath, worldPath, mdlPath, filename);
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

double test(Config config, char* worldPath, char* testFeaturePath, char* modelPath, char* filename)
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
