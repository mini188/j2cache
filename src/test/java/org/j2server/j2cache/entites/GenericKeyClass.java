package org.j2server.j2cache.entites;

import java.io.Serializable;
import java.util.List;

public class GenericKeyClass<T> implements Serializable{
	private static final long serialVersionUID = 1505967573049775572L;
	private String name;
	private List<T> list;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
}