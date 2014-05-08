package com.support;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class InfoRecord {

	public static final String AUTH_INFO_FILENAME = "auth_info.txt";
	
	static PrintWriter writer = null;
	
	public static void WriteScoreInfo(ArrayList<HashMap<String, Object>> list, String authname) {
		String rootDir = Cfg.getInstance().getRootDir();
		
		FileOutputStream authfos = null;
		try {
			authfos = new FileOutputStream(rootDir + AUTH_INFO_FILENAME, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		writer = new PrintWriter(authfos);
		
		for (HashMap<String, Object> item : list) {
			String name = (String) item.get("name");
			Float score = (Float) item.get("score");
			writer.print("<" + name + "," + score + "> ");
		}
		writer.println(authname + ";");
		
		writer.close();
		try {
			authfos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
