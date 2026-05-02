package com.ambiental.bike_monitor_pwa.model;

import java.time.LocalDateTime;

public class Station {

    private String stationId;
    private String name;
    private int capacity;
    private int availableBikes;
    private StationStatus status;
    private LocalDateTime lastUpdate;

    public Station() {
    }

    public Station(String stationId, String name, int capacity, int availableBikes, StationStatus status, LocalDateTime lastUpdate) {
        this.stationId = stationId;
        this.name = name;
        this.capacity = capacity;
        this.availableBikes = availableBikes;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(int availableBikes) {
        this.availableBikes = availableBikes;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
