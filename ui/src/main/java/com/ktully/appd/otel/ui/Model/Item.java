package com.ktully.appd.otel.ui.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
	
	private int id;
	
	private String category;
	
	private String name;
	
	private Float price;
	
	private String image;
	
	// Setters
	public void setId(int id) {
		this.id = id;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPrice(Float price) {
		this.price = price;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
	// Getters
	public int getId() {
		return this.id;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Float getPrice() {
		return this.price;
	}
	
	public String getImage() {
		return this.image;
	}
	

}
