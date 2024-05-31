package com.boris.delivery.dto;

import java.io.Serializable;

public class OrderDTO implements Serializable {
    private String id;
    private String email;
    private String idBook;
    private Boolean inCart;
    private Boolean inDelivery;
    private double quantity;

    public OrderDTO(String id, String email, String idBook, Boolean inCart, Boolean inDelivery, double quantity) {
        this.id = id;
        this.email = email;
        this.idBook = idBook;
        this.inCart = inCart;
        this.inDelivery = inDelivery;
        this.quantity = quantity;
    }

    public OrderDTO() {
    }

    public String getId() {
        return id;
    }

    /**public void setId(String id) {
        this.id = id;
    }*/

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdBook() {
        return idBook;
    }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    public Boolean getInCart() {
        return inCart;
    }

    public void setInCart(Boolean inCart) {
        this.inCart = inCart;
    }

    public Boolean getInDelivery() {
        return inDelivery;
    }

    public void setInDelivery(Boolean inDelivery) {
        this.inDelivery = inDelivery;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
