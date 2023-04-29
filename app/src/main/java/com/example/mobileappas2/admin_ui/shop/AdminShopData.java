package com.example.mobileappas2.admin_ui.shop;

public class AdminShopData {
    // private variables
    private String title, description;
    private Float price;
    private Integer ID;
	
	// constructor for the data
    public AdminShopData(String val1, String val2, Float val3, Integer val4)
    {
        title = val1;
        description = val2;
        price = val3;
        ID = val4;
    }

	// GETTERS & SETTERS
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }
}
