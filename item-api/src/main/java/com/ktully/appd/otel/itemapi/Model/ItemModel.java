package com.ktully.appd.otel.itemapi.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

@Entity
public class ItemModel {
	
	@Id
	@GeneratedValue(strategy=javax.persistence.GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false)
	private String category;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private Float price;
	
	@Column(nullable = true)
	private String image;
	
	// Setters
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
