package com.ixale.starparse.domain;

import java.io.Serializable;

public class Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	String name;
	Long guid;

	Entity() {
	}

	public Entity(String name, Long guid) {
		this.name = name;
		this.guid = guid;
	}

	public String getName() {
		return name;
	}

	public Long getGuid() {
		return guid;
	}

	public String toString() {
		return name + (guid != null ? " [" + guid + "]" : "");
	}
}
