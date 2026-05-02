package com.prac6.MetricService.service;

import com.prac6.MetricService.client.ScalingClient;
import com.prac6.MetricService.dto.MetricRequest;
import com.prac6.MetricService.dto.MetricResponse;
import com.prac6.MetricService.dto.ScalingRequest;
import com.prac6.MetricService.dto.ScalingResponse;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    private final ScalingClient scalingClient;

    public MetricService(ScalingClient scalingClient) {
        this.scalingClient = scalingClient;
    }

    public MetricResponse processMetric(MetricRequest request) {
        String status = calculateStatus(request.getType(), request.getValue());

        ScalingResponse scalingResponse = null;
        if (!"NORMAL".equalsIgnoreCase(status)) {
            scalingResponse = scalingClient.evaluate(
                    new ScalingRequest(request.getType(), status, request.getValue())
            );
        }

        MetricResponse response = new MetricResponse();
        response.setNodeId(request.getNodeId());
        response.setType(request.getType());
        response.setValue(request.getValue());
        response.setStatus(status);

        if (scalingResponse != null) {
            response.setScalingAction(scalingResponse.getAction());
            response.setScalingMessage(scalingResponse.getMessage());
        }

        return response;
    }

    private String calculateStatus(String type, int value) {
        if (type == null) return "UNKNOWN";

        return switch (type.toUpperCase()) {
            case "VM" -> {
                if (value < 30) yield "LOW";
                if (value > 80) yield "CRITICAL";
                yield "NORMAL";
            }
            case "CONTAINER" -> {
                if (value < 20) yield "LOW";
                if (value > 70) yield "CRITICAL";
                yield "NORMAL";
            }
            case "DATABASE" -> {
                if (value > 90) yield "CRITICAL";
                yield "NORMAL";
            }
            default -> "NORMAL";
        };
    }
}