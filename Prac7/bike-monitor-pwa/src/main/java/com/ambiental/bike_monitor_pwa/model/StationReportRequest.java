package com.ambiental.bike_monitor_pwa.model;

public class StationReportRequest {

    private String stationId;
    private int availableBikes;

    public StationReportRequest() {
    }

    public StationReportRequest(String stationId, int availableBikes) {
        this.stationId = stationId;
        this.availableBikes = availableBikes;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public int getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(int availableBikes) {
        this.availableBikes = availableBikes;
    }
}
