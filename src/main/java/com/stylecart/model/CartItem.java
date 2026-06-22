package com.stylecart.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private String productName;

    private double price;

    private String imageUrl;

    private int quantity = 1;

    private Long userId;

private String customerName;

    public CartItem() {
    }

    public CartItem(Long id, String productName, String imageUrl, double price,Long userId,
                String customerName) {
        this.id = id;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = 1;
        this.userId = userId;
this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public int getQuantity() {
    return quantity;
    }

    public void setQuantity(int quantity) {
    this.quantity = quantity;
    }

    public Long getUserId() {
    return userId;
}

public void setUserId(Long userId) {
    this.userId = userId;
}

public String getCustomerName() {
    return customerName;
}

public void setCustomerName(String customerName) {
    this.customerName = customerName;
}
}