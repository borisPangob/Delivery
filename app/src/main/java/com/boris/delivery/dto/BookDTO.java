package com.boris.delivery.dto;

public class BookDTO {
    private String id;
    private String title;
    private String summary;
    private String author;
    private long image;
    private double rating;
    private double price;

    public BookDTO(String id, String title, String summary, String author, long image, double rating, double price) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.author = author;
        this.image = image;
        this.rating = rating;
        this.price = price;
    }

    public BookDTO() {
    }

    public String getId() {
        return id;
    }

    /**public void setId(String id) {
        this.id = id;
    }*/

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getImage() {
        return image;
    }

    public void setImage(long image) {
        this.image = image;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
