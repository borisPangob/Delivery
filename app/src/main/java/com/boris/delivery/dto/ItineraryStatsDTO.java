package com.boris.delivery.dto;

public class ItineraryStatsDTO {
    private String distance;
    private String duration;

    public ItineraryStatsDTO(String distance, String duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public ItineraryStatsDTO() {
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "distance='" + distance + '\'' +
                ", duration='" + duration + '\''
                ;
    }
}
