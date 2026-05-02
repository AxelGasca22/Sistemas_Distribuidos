package com.ambiental.bike_monitor_pwa.controller;

import com.ambiental.bike_monitor_pwa.dto.DashboardSummary;
import com.ambiental.bike_monitor_pwa.model.Station;
import com.ambiental.bike_monitor_pwa.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ambiental.bike_monitor_pwa.model.StationReportRequest;
import com.ambiental.bike_monitor_pwa.model.StationStatus;
import com.ambiental.bike_monitor_pwa.model.StationRegisterRequest;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping("/stations/register")
    public ResponseEntity<Station> registerStation(@RequestBody StationRegisterRequest request) {
        Station station = stationService.registerStation(request);
        return ResponseEntity.ok(station);
    }

    @PostMapping("/stations/report")
    public ResponseEntity<?> reportAvailability(@RequestBody StationReportRequest request) {
        try {
            Station station = stationService.reportAvailability(request);
            return ResponseEntity.ok(station);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {
        return ResponseEntity.ok(stationService.getDashboardSummary());
    }
}
