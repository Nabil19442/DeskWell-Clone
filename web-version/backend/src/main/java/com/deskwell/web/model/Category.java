package com.deskwell.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String color = "#607D8B";

    @Column(name = "is_default")
    private Boolean isDefault = true;

    private String icon;

    @Column(name = "productivity_weight")
    private Double productivityWeight = 0.0;

    public Category() {}

    public Category(String name, String color, Boolean isDefault, Double productivityWeight) {
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
        this.productivityWeight = productivityWeight;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Double getProductivityWeight() { return productivityWeight; }
    public void setProductivityWeight(Double productivityWeight) { this.productivityWeight = productivityWeight; }
}
