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
	
	public int getId() {
		return this.id;
	}
	
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
	
	// Getters
	
	public String getCategory() {
		return this.category;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Float getPrice() {
		return this.price;
	}

}
