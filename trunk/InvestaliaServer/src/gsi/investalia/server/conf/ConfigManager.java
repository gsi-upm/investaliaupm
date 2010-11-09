package gsi.investalia.server.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
	public static final String CONFIG_NAME = "db.properties";
	
	public static final String PROJECT_DIR = "./projects";
	public static final String PROJECT_NAME = "investalia.properties";
	
	public static final String DATABASE_URL = "database_url";
	public static final String DATABASE_NAME = "database_name";
	public static final String DATABASE_USER = "database_user";
	public static final String DATABASE_PASS = "database_pass";
	public static final String PROJECT_HOME = "project-home";
	private static Properties props = readFile();

	private static Properties getProperties(String dir, String name) {
		props = new Properties();
		File file = new File(dir, name);
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
	
	private static Properties readFile() {
		Properties invProps = getProperties(PROJECT_DIR, PROJECT_NAME);
		String config_dir = invProps.getProperty(PROJECT_HOME) + "/cfg";
		return getProperties(config_dir, CONFIG_NAME);
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
