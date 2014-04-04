package com.support;

public class Config {
	
	static String rootDir = ".";
	static String dirLists[] = {"feature", "model", "world", "testFeature", "raw"};
	static String worldModelFile = "WorldModel.mdl";
	static String worldModelPath = "world";
	static String featurePath = "feature";
	static String modelPath = "model";
	static String testFeaturePath = "testFeature";
	static String rawPath = "raw";
	
	static String userName = "1";
	
	public enum DOTYPE {
		NONE,
		TRAIN,
		RECOGNIZE,
	}
	
	static DOTYPE type = DOTYPE.NONE;
	
	/**
	 * raw文件后缀名
	 */
	static String rawSuf = ".raw";
	/**
	 * feature文件后缀名
	 */
	static String feaSuf = ".mfcc";
	
	public static String getRootDir() {
		return rootDir;
	}
	public static void setRootDir(String rootDir) {
		Config.rootDir = rootDir;
	}
	public static String[] getDirLists() {
		return dirLists;
	}
	public static void setDirLists(String[] dirLists) {
		Config.dirLists = dirLists;
	}
	public static String getWorldModelFile() {
		return worldModelFile;
	}
	public static void setWorldModelFile(String worldModelFile) {
		Config.worldModelFile = worldModelFile;
	}
	public static String getWorldModelPath() {
		return worldModelPath;
	}
	public static void setWorldModelPath(String worldModelPath) {
		Config.worldModelPath = worldModelPath;
	}
	public static String getFeaturePath() {
		return featurePath;
	}
	public static void setFeaturePath(String featureDir) {
		Config.featurePath = featureDir;
	}
	public static String getModelPath() {
		return modelPath;
	}
	public static void setModelPath(String modelDir) {
		Config.modelPath = modelDir;
	}
	public static String getTestFeaturePath() {
		return testFeaturePath;
	}
	public static void setTestFeaturePath(String testFeatureDir) {
		Config.testFeaturePath = testFeatureDir;
	}
	public static String getUserName() {
		return userName;
	}
	public static void setUserName(String userName) {
		Config.userName = userName;
	}
	public static String getRawPath() {
		return rawPath;
	}
	public static void setRawPath(String rawPath) {
		Config.rawPath = rawPath;
	}
	public static String getRawSuf() {
		return rawSuf;
	}
	public static void setRawSuf(String rawSuf) {
		Config.rawSuf = rawSuf;
	}
	public static String getFeaSuf() {
		return feaSuf;
	}
	public static void setFeaSuf(String feaSuf) {
		Config.feaSuf = feaSuf;
	}
	public static DOTYPE getType() {
		return type;
	}
	public static void setType(DOTYPE type) {
		Config.type = type;
	}
	
}
