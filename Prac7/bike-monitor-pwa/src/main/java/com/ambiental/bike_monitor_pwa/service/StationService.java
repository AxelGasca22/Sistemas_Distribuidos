package com.ambiental.bike_monitor_pwa.service;

import com.ambiental.bike_monitor_pwa.dto.DashboardSummary;
import com.ambiental.bike_monitor_pwa.model.Station;
import com.ambiental.bike_monitor_pwa.model.StationReportRequest;
import com.ambiental.bike_monitor_pwa.model.StationStatus;
import com.ambiental.bike_monitor_pwa.model.StationRegisterRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StationService {

    private final ConcurrentHashMap<String, Station> stations = new ConcurrentHashMap<>();

    public Station registerStation(StationRegisterRequest request) {
        int initialBikes = request.getCapacity() / 2;

        Station station = new Station(
                request.getStationId(),
                request.getName(),
                request.getCapacity(),
                initialBikes,
                calculateStatus(initialBikes, request.getCapacity()),
                LocalDateTime.now()
        );

        stations.put(request.getStationId(), station);
        return station;
    }

    public Station reportAvailability(StationReportRequest request) {
        Station station = stations.get(request.getStationId());

        if (station == null) {
            throw new IllegalArgumentException("La estación no existe: " + request.getStationId());
        }

        int availableBikes = Math.max(0, Math.min(request.getAvailableBikes(), station.getCapacity()));

        station.setAvailableBikes(availableBikes);
        station.setStatus(calculateStatus(availableBikes, station.getCapacity()));
        station.setLastUpdate(LocalDateTime.now());

        return station;
    }

    public List<Station> getAllStations() {
        updateOfflineStations();

        List<Station> stationList = new ArrayList<>(stations.values());
        stationList.sort(Comparator.comparing(Station::getStationId));

        return stationList;
    }

    public DashboardSummary getDashboardSummary() {
        updateOfflineStations();

        List<Station> stationList = new ArrayList<>(stations.values());

        int totalStations = stationList.size();
        int availableStations = 0;
        int lowStations = 0;
        int emptyStations = 0;
        int offlineStations = 0;
        int totalAvailableBikes = 0;

        for (Station station : stationList) {
            if (station.getStatus() == StationStatus.AVAILABLE) {
                availableStations++;
            } else if (station.getStatus() == StationStatus.LOW) {
                lowStations++;
            } else if (station.getStatus() == StationStatus.EMPTY) {
                emptyStations++;
            } else if (station.getStatus() == StationStatus.OFFLINE) {
                offlineStations++;
            }

            if (station.getStatus() != StationStatus.OFFLINE) {
                totalAvailableBikes += station.getAvailableBikes();
            }
        }

        return new DashboardSummary(
                totalStations,
                availableStations,
                lowStations,
                emptyStations,
                offlineStations,
                totalAvailableBikes
        );
    }

    private StationStatus calculateStatus(int availableBikes, int capacity) {
        if (availableBikes <= 0) {
            return StationStatus.EMPTY;
        }

        double percentage = (double) availableBikes / capacity;

        if (percentage <= 0.25) {
            return StationStatus.LOW;
        }

        return StationStatus.AVAILABLE;
    }

    private void updateOfflineStations() {
        LocalDateTime now = LocalDateTime.now();

        for (Station station : stations.values()) {
            long secondsWithoutUpdate = Duration.between(station.getLastUpdate(), now).getSeconds();

            if (secondsWithoutUpdate > 15) {
                station.setStatus(StationStatus.OFFLINE);
            }
        }
    }
}
