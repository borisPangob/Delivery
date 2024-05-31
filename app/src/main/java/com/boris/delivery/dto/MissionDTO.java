package com.boris.delivery.dto;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;

public class MissionDTO {
    private Date date;
    private String delivererEmail;
    private boolean isAccepted;
    private boolean isRealised;
    private ArrayList<String> listOfAdrresses;
    private ArrayList<GeoPoint> listOfGeopoints;
    private String id;

    public MissionDTO(Date date, String delivererEmail, boolean isAccepted, boolean isRealised, ArrayList<String> listOfAdrresses, ArrayList<GeoPoint> listOfGeopoints, String id) {
        this.date = date;
        this.delivererEmail = delivererEmail;
        this.isAccepted = isAccepted;
        this.isRealised = isRealised;
        this.listOfAdrresses = listOfAdrresses;
        this.listOfGeopoints = listOfGeopoints;
        this.id = id;
    }

    public MissionDTO() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public boolean isRealised() {
        return isRealised;
    }

    public void setRealised(boolean realised) {
        isRealised = realised;
    }

    public ArrayList<String> getListOfAdrresses() {
        return listOfAdrresses;
    }

    public void setListOfAdrresses(ArrayList<String> listOfAdrresses) {
        this.listOfAdrresses = listOfAdrresses;
    }

    public ArrayList<GeoPoint> getListOfGeopoints() {
        return listOfGeopoints;
    }

    public void setListOfGeopoints(ArrayList<GeoPoint> listOfGeopoints) {
        this.listOfGeopoints = listOfGeopoints;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
