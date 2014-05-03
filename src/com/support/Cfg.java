package com.support;

public class Cfg {
	
	String rootDir = ".";
	String dirLists[] = {"admin", "world", "tmp", "users"};
	
	String tmpPath = "tmp";
	String worldMdlPath = "world";
	String adminPath = "admin";
	String usersPath = "users";
	String worldMdlFile = "WorldModel.mdl";
	
	static Cfg Instance = new Cfg();
	public static Cfg getInstance() {
		return Instance;
	}
	private Cfg(){}
	
	public String getUsersPath() {
		return usersPath;
	}
	public void setUsersPath(String usersPath) {
		this.usersPath = usersPath;
	}
	public String getRootDir() {
		return rootDir;
	}
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	public String[] getDirLists() {
		return dirLists;
	}
	public void setDirLists(String[] dirLists) {
		this.dirLists = dirLists;
	}
	public String getTmpPath() {
		return tmpPath;
	}
	public void setTmpPath(String tmpPath) {
		this.tmpPath = tmpPath;
	}
	public String getWorldMdlPath() {
		return worldMdlPath;
	}
	public void setWorldMdlPath(String worldMdlPath) {
		this.worldMdlPath = worldMdlPath;
	}
	public String getAdminPath() {
		return adminPath;
	}
	public void setAdminPath(String adminPath) {
		this.adminPath = adminPath;
	}
	public String getWorldMdlFile() {
		return worldMdlFile;
	}
	public void setWorldMdlFile(String worldMdlFile) {
		this.worldMdlFile = worldMdlFile;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFeaSuf() {
		return feaSuf;
	}
	public void setFeaSuf(String feaSuf) {
		this.feaSuf = feaSuf;
	}
	public String getMdlSuf() {
		return mdlSuf;
	}
	public void setMdlSuf(String mdlSuf) {
		this.mdlSuf = mdlSuf;
	}
	String userName = "admin";
	String feaSuf = ".mfcc";
	String mdlSuf = ".mdl";
	
	
}
