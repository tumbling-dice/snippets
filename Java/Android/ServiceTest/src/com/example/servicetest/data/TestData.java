package com.example.servicetest.data;

import java.io.Serializable;

public class TestData implements Serializable {

	private static final long serialVersionUID = -8710373208447953446L;

	private String name;
	private String value;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public boolean equals(Object o) {
		if(o != null && !(o instanceof TestData)) return false;
		return this.name.equals(((TestData)o).getName());
	}
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	@Override
	public String toString() {
		return this.name;
	}


}
