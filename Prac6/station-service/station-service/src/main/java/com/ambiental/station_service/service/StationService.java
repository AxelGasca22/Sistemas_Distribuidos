package com.ambiental.station_service.service;

import com.ambiental.station_service.model.MeasurementRequest;
import com.ambiental.station_service.model.Station;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StationService {

    private final Map<String, Station> stations = new ConcurrentHashMap<>();

    public Station registerStation(Station station) {
        station.setStatus("ACTIVE");
        station.setLastHeartbeat(LocalDateTime.now());
        stations.put(station.getStationId(), station);
        return station;
    }

    public Station heartbeat(String stationId) {
        Station station = stations.get(stationId);

        if (station == null) {
            return null;
        }

        station.setStatus("ACTIVE");
        station.setLastHeartbeat(LocalDateTime.now());
        return station;
    }

    public Station saveMeasurement(MeasurementRequest request) {
        Station station = stations.get(request.getStationId());

        if (station == null) {
            return null;
        }

        station.setTemperature(request.getTemperature());
        station.setHumidity(request.getHumidity());
        station.setAirQuality(request.getAirQuality());

        if (request.getAirQuality() != null && request.getAirQuality() > 80) {
            station.setStatus("WARNING");
        } else {
            station.setStatus("ACTIVE");
        }

        return station;
    }

    public Collection<Station> getAllStations() {
        return stations.values();
    }

    public String getSystemStatus() {
        long total = stations.size();
        long active = stations.values().stream()
                .filter(station -> "ACTIVE".equals(station.getStatus()))
                .count();
        long warning = stations.values().stream()
                .filter(station -> "WARNING".equals(station.getStatus()))
                .count();

        return "Total stations: " + total +
                ", Active: " + active +
                ", Warning: " + warning;
    }
}
