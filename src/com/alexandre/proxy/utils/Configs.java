package com.alexandre.proxy.utils;

import java.util.HashMap;

public class Configs {
	
	private HashMap<String, Configs> subconfigs = new HashMap<>();
	private String value;
	public String name;
	public Configs parent;
	
	public Configs(Configs parent, String name, String value) {
		this.parent = parent;
		this.name = name;
		this.value = value;
	}
	
	public Configs addSubConfig(String name, String value) {
		Configs subconfig = new Configs(this, name, value);
		this.subconfigs.put(name, subconfig);
		return subconfig;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public int getIntValue() {
		return Integer.parseInt(this.value);
	}
	
	public float getFloatValue() {
		return Float.parseFloat(this.value);
	}
	
	public boolean getBooleanValue() {
		return Boolean.parseBoolean(this.value);
	}
	
	public HashMap<String, Configs> getAllSubConfigs() {
		return this.subconfigs;
	}
	
	public Configs getSubConfigs(String name) {
		return this.subconfigs.get(name);
	}
	
	public HashMap<String, String> listAllPath() {
		HashMap<String, String> paths = new HashMap<>();
		
		if (!this.value.isEmpty()) paths.put(this.getPath(), this.value);
		for (Configs config : this.subconfigs.values()) {
			paths.putAll(config.listAllPath());
		}
		
		return paths;
	}
	
	public String getPath() {
		StringBuilder path = new StringBuilder();
		Configs current = this;
		while (current != null) {
			if (path.length() > 0) path.insert(0, ".");
			path.insert(0, current.name);
			
			current = current.parent;
		}
		
		return path.toString();
	}
}
