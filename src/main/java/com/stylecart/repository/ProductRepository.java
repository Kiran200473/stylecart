package com.stylecart.repository;

import com.stylecart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByCategory(String category);
}