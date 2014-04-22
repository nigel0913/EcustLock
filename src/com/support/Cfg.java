package com.support;

public class Cfg {
	
	static String rootDir = ".";
	static String dirLists[] = {"admin", "world", "tmp"};
	
	static String tmpPath = "tmp";
	static String worldMdlPath = "world";
	static String adminPath = "admin";
	
	static String worldMdlFile = "WorldModel.mdl";
	static String userName = "admin";
	static String feaSuf = ".mfcc";
	static String mdlSuf = ".mdl";
	
	public static String getMdlSuf() {
		return mdlSuf;
	}
	public static void setMdlSuf(String mdlSuf) {
		Cfg.mdlSuf = mdlSuf;
	}
	public static String getTmpPath() {
		return tmpPath;
	}
	public static void setTmpPath(String tmpPath) {
		Cfg.tmpPath = tmpPath;
	}
	public static String getWorldMdlPath() {
		return worldMdlPath;
	}
	public static void setWorldMdlPath(String worldMdlPath) {
		Cfg.worldMdlPath = worldMdlPath;
	}
	public static String getAdminPath() {
		return adminPath;
	}
	public static void setAdminPath(String adminPath) {
		Cfg.adminPath = adminPath;
	}
	public static String getWorldMdlFile() {
		return worldMdlFile;
	}
	public static void setWorldMdlFile(String worldMdlFile) {
		Cfg.worldMdlFile = worldMdlFile;
	}
	
	public static String getRootDir() {
		return rootDir;
	}
	public static void setRootDir(String rootDir) {
		Cfg.rootDir = rootDir;
	}
	public static String[] getDirLists() {
		return dirLists;
	}
	public static void setDirLists(String[] dirLists) {
		Cfg.dirLists = dirLists;
	}
	public static String getUserName() {
		return userName;
	}
	public static void setUserName(String userName) {
		Cfg.userName = userName;
	}
	public static String getFeaSuf() {
		return feaSuf;
	}
	public static void setFeaSuf(String feaSuf) {
		Cfg.feaSuf = feaSuf;
	}
	
}
