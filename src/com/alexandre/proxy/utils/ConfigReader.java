package com.alexandre.proxy.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigReader {

	public static int spaceNumber = 4;
	
	public static Configs read(File file) {
		try {
			if (!file.exists()) {
				System.err.println("[YML] file " + file.getAbsolutePath() + " not found.");
				return null;
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			String line;
			int space = 0;
			Configs lastConfig = null;
			Configs firstConfig = null;
			while ((line = br.readLine()) != null) {
				line = line.replace("\t", "    ");
				if (line.replaceAll(" ", "").isEmpty() || line.replaceAll(" ", "").startsWith("#")) continue;
				int spaceCount = getSpaceCount(line);
				String name = name(line, spaceCount);
				String value = value(line);
				
				if (firstConfig == null) {
					firstConfig = new Configs(null, name, value);
					lastConfig = firstConfig;
					continue;
				}
				
				if (spaceCount > space) {
					lastConfig = lastConfig.addSubConfig(name, value);
				} else if (spaceCount == space) {
					lastConfig = lastConfig.parent.addSubConfig(name, value);
				} else {
					Configs parent = lastConfig;
					for (int i = 0; i < space - spaceCount; i++) parent = parent.parent;
					lastConfig = parent.parent.addSubConfig(name, value);
				}
				
				space = spaceCount;
			}
			br.close();

			return firstConfig;
		} catch (IOException e) {
			System.err.println("[Lang] Error while reading");
			System.err.println(e.getMessage());
			return null;
		}
	}

	public static String name(String line, int spaceCount) {
		String name;
		name = line.split(":")[0];
		name = name.substring(spaceCount * spaceNumber);
		
		return name;
	}
	public static String value(String line) {
		String value;

		if (line.split(":").length <= 1) return "";

		value = line.replace(line.split(":")[0] + ":", "");
		if (value.startsWith("\"")) value = value.substring(1);
		if (value.endsWith("\"")) value = value.substring(0, value.length() - 1);

		return value;
	}
	
	public static int getSpaceCount(String line) {
		int count = 0;
		
		while (line.startsWith(" ")) {
			line = line.substring(1);
			count++;
		}
		
		return count / spaceNumber;
	}
}
