package com.amazonaws.lambda.demo;

public class EmployeeDataBean {
	
	private String name;
	private int id;
	private String country;

	public EmployeeDataBean(String name, int id, String country) {
		this.name = name;
		this.id = id;
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
