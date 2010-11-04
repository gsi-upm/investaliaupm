package gsi.investalia.server.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
	public static final String FILE_NAME = "db.properties";
	public static final String FILE_DIR = "./cfg"; // Project home 
	
	public static final String DATABASE_URL = "database_url";
	public static final String DATABASE_NAME = "database_name";
	public static final String DATABASE_USER = "database_user";
	public static final String DATABASE_PASS = "database_pass";
	private static Properties props = readFile();

	private static Properties readFile() {
		props = new Properties();
		File file = new File(FILE_DIR, FILE_NAME);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props;
	}
	
	public static String getDatabaseUser() {
		return props.getProperty(DATABASE_USER);
	}
	
	public static String getDatabasePass() {
		return props.getProperty(DATABASE_PASS);
	}
	
	public static String getDatabaseName() {
		return props.getProperty(DATABASE_NAME);
	}
	
	public static String getDatabaseUrl() {
		return props.getProperty(DATABASE_URL);
	}
	
	// Test method
	public static void main(String args[]) {
		System.out.println(getDatabaseUser());
		System.out.println(getDatabasePass());
		System.out.println(getDatabaseName());
		System.out.println(getDatabaseUrl());
	}
}
