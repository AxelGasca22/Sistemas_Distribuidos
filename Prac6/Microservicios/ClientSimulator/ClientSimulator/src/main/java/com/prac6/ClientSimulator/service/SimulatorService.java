package com.prac6.ClientSimulator.service;

import com.prac6.ClientSimulator.dto.HeartbeatRequest;
import com.prac6.ClientSimulator.dto.MetricRequest;
import com.prac6.ClientSimulator.dto.RegisterRequest;
import com.prac6.ClientSimulator.model.SimulatedNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class SimulatorService {

    private final RestTemplate restTemplate;
    private final Random random = new Random();
    private final List<SimulatedNode> nodes = new ArrayList<>();

    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    @Value("${simulator.node-count}")
    private int nodeCount;

    public SimulatorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        createNodes();
        registerNodes();
        startSimulation();
    }

    private void createNodes() {
        String[] types = {"VM", "CONTAINER", "DATABASE", "STORAGE-NODE"};

        for (int i = 0; i < nodeCount; i++) {
            String type = types[random.nextInt(types.length)];
            String nodeId = type + "-" + UUID.randomUUID().toString().substring(0, 8);
            nodes.add(new SimulatedNode(nodeId, type, "METRIC"));
        }
    }

    private void registerNodes() {
        for (SimulatedNode node : nodes) {
            try {
                RegisterRequest request = new RegisterRequest(
                        node.getNodeId(),
                        node.getType(),
                        node.getRole()
                );

                restTemplate.postForObject(
                        gatewayBaseUrl + "/registry/register",
                        request,
                        Object.class
                );

                System.out.println("[REGISTER] " + node.getNodeId() + " type=" + node.getType());

            } catch (Exception e) {
                System.err.println("[REGISTER ERROR] " + node.getNodeId() + " -> " + e.getMessage());
            }
        }
    }

    private void startSimulation() {
        for (SimulatedNode node : nodes) {
            Thread heartbeatThread = new Thread(() -> runHeartbeatLoop(node));
            Thread metricThread = new Thread(() -> runMetricLoop(node));

            heartbeatThread.start();
            metricThread.start();
        }
    }

    private void runHeartbeatLoop(SimulatedNode node) {
        while (true) {
            try {
                restTemplate.postForObject(
                        gatewayBaseUrl + "/registry/heartbeat",
                        new HeartbeatRequest(node.getNodeId()),
                        Object.class
                );

                System.out.println("[HEARTBEAT] " + node.getNodeId());

                Thread.sleep(5000);
            } catch (Exception e) {
                System.err.println("[HEARTBEAT ERROR] " + node.getNodeId() + " -> " + e.getMessage());
            }
        }
    }

    private void runMetricLoop(SimulatedNode node) {
        while (true) {
            try {
                int value = generateMetricValue(node.getType());

                restTemplate.postForObject(
                        gatewayBaseUrl + "/metrics/report",
                        new MetricRequest(node.getNodeId(), node.getType(), value),
                        Object.class
                );

                System.out.println("[METRIC] " + node.getNodeId() +
                        " type=" + node.getType() +
                        " value=" + value);

                Thread.sleep(4000);
            } catch (Exception e) {
                System.err.println("[METRIC ERROR] " + node.getNodeId() + " -> " + e.getMessage());
            }
        }
    }

    private int generateMetricValue(String type) {
        return switch (type) {
            case "VM" -> random.nextInt(101);
            case "CONTAINER" -> random.nextInt(101);
            case "DATABASE" -> random.nextInt(121);
            case "STORAGE-NODE" -> random.nextInt(101);
            default -> random.nextInt(100);
        };
    }
}