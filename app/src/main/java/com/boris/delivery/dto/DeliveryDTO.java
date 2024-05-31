package com.boris.delivery.dto;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Date;

public class DeliveryDTO implements Serializable {
    private String address;
    private String email;
    private Date timestamp;
    private String delivererEmail;
    private boolean isAccepted;
    private boolean isAttributed;
    private GeoPoint location;
    private String Ref;
    private Double total;

    public DeliveryDTO(String address, String email, String Ref, Date timestamp, String delivererEmail, boolean isAccepted, boolean isAttributed, GeoPoint location, Double total) {
        this.address = address;
        this.email = email;
        this.Ref = Ref;
        this.timestamp = timestamp;
        this.delivererEmail = delivererEmail;
        this.isAccepted = isAccepted;
        this.isAttributed = isAttributed;
        this.location = location;
        this.total = total;
    }

    public DeliveryDTO() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRef() {
        return Ref;
    }

    public void setRef(String Ref) {
        this.Ref = Ref;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDelivererEmail() {
        return delivererEmail;
    }

    public void setDelivererEmail(String delivererEmail) {
        this.delivererEmail = delivererEmail;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isAttributed() {
        return isAttributed;
    }

    public void setDelivered(boolean attributed) {
        isAttributed = attributed;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

}
