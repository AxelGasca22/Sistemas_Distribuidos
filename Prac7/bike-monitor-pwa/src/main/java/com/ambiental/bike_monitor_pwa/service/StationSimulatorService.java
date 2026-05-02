package com.ambiental.bike_monitor_pwa.service;

import com.ambiental.bike_monitor_pwa.dto.DashboardSummary;
import com.ambiental.bike_monitor_pwa.model.Station;
import com.ambiental.bike_monitor_pwa.model.StationReportRequest;
import com.ambiental.bike_monitor_pwa.model.StationStatus;
import com.ambiental.bike_monitor_pwa.model.StationRegisterRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class StationSimulatorService {

    private final StationService stationService;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    public StationSimulatorService(StationService stationService) {
        this.stationService = stationService;
    }

    @PostConstruct
    public void startSimulation() {
        registerInitialStations();

        scheduler.scheduleAtFixedRate(() -> simulateStation("EST-01", 15), 2, 4, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> simulateStation("EST-02", 12), 3, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> simulateStation("EST-03", 20), 4, 6, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> simulateStation("EST-04", 10), 5, 7, TimeUnit.SECONDS);
    }

    private void registerInitialStations() {
        stationService.registerStation(new StationRegisterRequest("EST-01", "Estación Centro", 15));
        stationService.registerStation(new StationRegisterRequest("EST-02", "Estación Universidad", 12));
        stationService.registerStation(new StationRegisterRequest("EST-03", "Estación Parque", 20));
        stationService.registerStation(new StationRegisterRequest("EST-04", "Estación Metro", 10));
    }

    private void simulateStation(String stationId, int capacity) {
        try {
            int availableBikes = random.nextInt(capacity + 1);

            stationService.reportAvailability(
                    new StationReportRequest(stationId, availableBikes)
            );

            System.out.println("[SIMULADOR] " + stationId + " reportó " + availableBikes + " bicicletas disponibles");

        } catch (Exception e) {
            System.out.println("[ERROR SIMULADOR] " + e.getMessage());
        }
    }
}
