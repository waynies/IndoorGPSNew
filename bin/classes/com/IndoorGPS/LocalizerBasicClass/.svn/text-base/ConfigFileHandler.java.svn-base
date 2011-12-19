package com.IndoorGPS.LocalizerBasicClass;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.StreamHandler;

import com.IndoorGPS.Utilities;


public class ConfigFileHandler{
	public String filename;
	public boolean fileExist;
	public HashMap<String, String> paramList;
	
	public ConfigFileHandler(String myfile){
		this.filename = myfile;
		this.fileExist = Utilities.IsValidFilePath(this.filename);
		
		this.paramList = new HashMap<String, String>();
	}
	
	public void Dispose(){
		this.paramList.clear();
	}
	
	public void ParseSettingFile(){
	
		if(!this.fileExist){
			return;
		}
		
		String line;
		String[] splitstrings;
		
		
		
		
			
	}
	
	public boolean IsDefined(String key){
		return this.paramList.containsKey(key);
	}
	
	public String GetValueString(String key){
		return this.paramList.get(key);	
	}
	
	public int GetValueInt(String key){
		return Integer.parseInt(this.paramList.get(key));
	}
	
	public double GetValueDouble(String key){
		return Double.parseDouble(this.paramList.get(key));
	}
	
	public boolean GetValueBool(String key){
		if(this.paramList.get(key) == "1"){
			return true;
		}
		else{
			return false;
		}	
	}
	
}
