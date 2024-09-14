package com.lestarieragemilang.desktop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", unique = true, nullable = false)
    private String categoryId;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(name = "product_type", nullable = false, length = 50)
    private String productType;

    @Column(length = 20)
    private String size;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "weight_unit", length = 20)
    private String weightUnit;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }
}