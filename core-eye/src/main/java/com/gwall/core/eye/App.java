package com.gwall.core.eye;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import com.gwall.core.eye.sysinfo.SystemInfo;
import com.gwall.core.eye.sysinfo.SystemInfoImpl;

public class App {
	
	public static void main(String[] args) {
		
		try {
			String ss = DigestUtils.sha512Hex("bca0ffad337e44719e6a896f60b54c26");
			System.out.println(DigestUtils.md5Hex(new FileInputStream(
					new File("C:\\Users\\Administrator\\Desktop\\core-common-1.0.0.1-SNAPSHOT.jar"))));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		/*
		SystemInfo sys = new SystemInfoImpl();
		System.out.println(sys.getKey());
		for(int i=0;i<10;i++){
			try{Thread.sleep(1000);}catch(Exception e){}
			System.out.println(sys.getAllJson());
		}*/
	}

}
