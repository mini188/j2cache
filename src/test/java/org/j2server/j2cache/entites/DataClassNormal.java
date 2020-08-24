package org.j2server.j2cache.entites;

import java.io.Serializable;

public class DataClassNormal implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private long value;
	private String strValue;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public String getStrValue() {
		return strValue;
	}
	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}
}