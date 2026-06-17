package com.stylecart.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Product {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String name;

private String imageUrl;

private double price;

private String category;

private String description;

private double rating;

private int discount;

public Product() {
}

public Product(Long id,
               String name,
               String imageUrl,
               double price,
               String category,
               String description,
               double rating,
               int discount) {

    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.price = price;
    this.category = category;
    this.description = description;
    this.rating = rating;
    this.discount = discount;
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public String getImageUrl() {
    return imageUrl;
}

public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
}

public double getPrice() {
    return price;
}

public void setPrice(double price) {
    this.price = price;
}

public String getCategory() {
    return category;
}

public void setCategory(String category) {
    this.category = category;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public double getRating() {
    return rating;
}

public void setRating(double rating) {
    this.rating = rating;
}

public int getDiscount() {
    return discount;
}

public void setDiscount(int discount) {
    this.discount = discount;
}

}
