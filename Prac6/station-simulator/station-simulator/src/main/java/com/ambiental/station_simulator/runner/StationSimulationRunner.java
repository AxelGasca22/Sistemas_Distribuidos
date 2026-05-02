package com.ambiental.station_simulator.runner;


import com.ambiental.station_simulator.model.MeasurementRequest;
import com.ambiental.station_simulator.model.StationRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class StationSimulationRunner implements CommandLineRunner {

    private static final String STATION_SERVICE_URL = "http://localhost:8081/stations";

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    private final List<StationRequest> stations = List.of(
            new StationRequest("STATION-CDMX-001", "Centro", "AIR_QUALITY"),
            new StationRequest("STATION-CDMX-002", "Norte", "TEMPERATURE"),
            new StationRequest("STATION-CDMX-003", "Sur", "HUMIDITY")
    );

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Iniciando simulador de estaciones ambientales ===");

        registerStations();

        while (true) {
            for (StationRequest station : stations) {
                sendHeartbeat(station.getStationId());
                sendMeasurement(station.getStationId());
            }

            Thread.sleep(5000);
        }
    }

    private void registerStations() {
        for (StationRequest station : stations) {
            try {
                restTemplate.postForObject(
                        STATION_SERVICE_URL + "/register",
                        station,
                        String.class
                );

                System.out.println("[REGISTER] Estación registrada: " + station.getStationId());
            } catch (Exception e) {
                System.out.println("[ERROR] No se pudo registrar la estación: " + station.getStationId());
                System.out.println(e.getMessage());
            }
        }
    }

    private void sendHeartbeat(String stationId) {
        try {
            Map<String, String> request = Map.of("stationId", stationId);

            restTemplate.postForObject(
                    STATION_SERVICE_URL + "/heartbeat",
                    request,
                    String.class
            );

            System.out.println("[HEARTBEAT] Señal enviada por: " + stationId);
        } catch (Exception e) {
            System.out.println("[ERROR] No se pudo enviar heartbeat de: " + stationId);
        }
    }

    private void sendMeasurement(String stationId) {
        try {
            MeasurementRequest measurement = new MeasurementRequest(
                    stationId,
                    generateTemperature(),
                    generateHumidity(),
                    generateAirQuality()
            );

            restTemplate.postForObject(
                    STATION_SERVICE_URL + "/measurement",
                    measurement,
                    String.class
            );

            System.out.println("[MEASUREMENT] " + stationId +
                    " temp=" + measurement.getTemperature() +
                    " hum=" + measurement.getHumidity() +
                    " airQuality=" + measurement.getAirQuality());
        } catch (Exception e) {
            System.out.println("[ERROR] No se pudo enviar medición de: " + stationId);
        }
    }

    private double generateTemperature() {
        return 18 + random.nextDouble() * 15;
    }

    private double generateHumidity() {
        return 30 + random.nextDouble() * 50;
    }

    private double generateAirQuality() {
        return 40 + random.nextDouble() * 60;
    }
}
