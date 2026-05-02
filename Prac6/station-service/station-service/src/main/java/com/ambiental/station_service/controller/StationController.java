package com.ambiental.station_service.controller;

import com.ambiental.station_service.model.MeasurementRequest;
import com.ambiental.station_service.model.Station;
import com.ambiental.station_service.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Station> registerStation(@RequestBody Station station) {
        return ResponseEntity.ok(stationService.registerStation(station));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody Map<String, String> request) {
        String stationId = request.get("stationId");
        Station station = stationService.heartbeat(stationId);

        if (station == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(station);
    }

    @PostMapping("/measurement")
    public ResponseEntity<?> saveMeasurement(@RequestBody MeasurementRequest request) {
        Station station = stationService.saveMeasurement(request);

        if (station == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(station);
    }

    @GetMapping
    public ResponseEntity<Collection<Station>> getStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/status")
    public ResponseEntity<String> getSystemStatus() {
        return ResponseEntity.ok(stationService.getSystemStatus());
    }
}
