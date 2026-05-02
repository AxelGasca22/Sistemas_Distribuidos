package com.ambiental.station_service.model;

public class MeasurementRequest {

    private String stationId;
    private Double temperature;
    private Double humidity;
    private Double airQuality;

    public MeasurementRequest() {
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
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
