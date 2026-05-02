package com.ambiental.bike_monitor_pwa.model;

public class StationRegisterRequest {

    private String stationId;
    private String name;
    private int capacity;

    public StationRegisterRequest() {
    }

    public StationRegisterRequest(String stationId, String name, int capacity) {
        this.stationId = stationId;
        this.name = name;
        this.capacity = capacity;
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
}
