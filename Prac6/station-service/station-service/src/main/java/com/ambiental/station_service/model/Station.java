package com.ambiental.station_service.model;

import java.time.LocalDateTime;

public class Station {

    private String stationId;
    private String zone;
    private String type;
    private String status;
    private LocalDateTime lastHeartbeat;

    private Double temperature;
    private Double humidity;
    private Double airQuality;

    public Station() {
    }

    public Station(String stationId, String zone, String type) {
        this.stationId = stationId;
        this.zone = zone;
        this.type = type;
        this.status = "ACTIVE";
        this.lastHeartbeat = LocalDateTime.now();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(Double airQuality) {
        this.airQuality = airQuality;
    }
}