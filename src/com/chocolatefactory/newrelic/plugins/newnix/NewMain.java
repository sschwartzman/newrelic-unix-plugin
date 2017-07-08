package com.chocolatefactory.newrelic.plugins.newnix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.Config;
import com.newrelic.metrics.publish.configuration.ConfigurationException;
import com.chocolatefactory.newrelic.plugins.newnix.NewNixAgentFactory;

public class NewMain {	
	
	public static void main(String[] args) {
		try {
			checkPluginFile();
	        Runner runner = new Runner();
	        runner.add(new NewNixAgentFactory());
	        runner.setupAndRun(); // Never returns
	    } catch (ConfigurationException e) {
	        System.err.println("ERROR: " + e.getMessage());
	        System.exit(-1);
	    }
	}
	
	private static void checkPluginFile() throws ConfigurationException {
		File pluginConfigFile = new File(Config.getConfigDirectory() + File.separator + "plugin.json");
		if(!pluginConfigFile.exists()) {
			System.err.println("WARNING: " + pluginConfigFile.toString() + " does not exist");
			String thisOS = System.getProperty("os.name").toLowerCase().trim();
			String thisVer = System.getProperty("os.version").trim();
			
			if(thisOS.contains("os x")) {
				thisOS = "osx";
			} else if(thisOS.contains("sunos")) {
				if(thisVer.equals("5.10")) {
					thisOS = "solaris_10";
				} else if(thisVer.equals("5.11")) {
					thisOS = "solaris_11";
				} else {
					throw new ConfigurationException("This version of Solaris isn't supported: " + thisVer);
				}
			}
			
			File specificPluginConfigFile = new File(Config.getConfigDirectory() + File.separator + "plugin.json." + thisOS);
			if(specificPluginConfigFile.exists()) {
				System.err.println("Attempting to copy plugin.json." + thisOS + " to plugin.json");
				try {
					copyFile(specificPluginConfigFile, pluginConfigFile);
					System.err.println("Successfully copied plugin.json." + thisOS + " to plugin.json");
				} catch(IOException ioe) {
					throw new ConfigurationException("Failed to copy plugin.json file for your OS: " + thisOS + "\n" + ioe.getMessage());
				}
			} else {
				throw new ConfigurationException("The plugin.json file for this OS doesn't exist: " + thisOS);
			}
		}
	}
	
	private static void copyFile(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    try {
	        OutputStream out = new FileOutputStream(dst);
	        try {
	            byte[] buf = new byte[1024];
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	        } finally {
	            out.close();
	        }
	    } finally {
	        in.close();
	    }
	}
}

