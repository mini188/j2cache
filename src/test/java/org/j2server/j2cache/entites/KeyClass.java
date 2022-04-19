package org.j2server.j2cache.entites;

import java.io.Serializable;

public class KeyClass implements Serializable {
	private static final long serialVersionUID = -8277034811358859160L;
	
	private String keyName;
	
	public KeyClass() {
		
	}
	
	public KeyClass(String keyName) {
		this.keyName = keyName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	

}
