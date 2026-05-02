package com.ambiental.station_simulator.model;

public class StationRequest {

    private String stationId;
    private String zone;
    private String type;

    public StationRequest() {
    }

    public StationRequest(String stationId, String zone, String type) {
        this.stationId = stationId;
        this.zone = zone;
        this.type = type;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
