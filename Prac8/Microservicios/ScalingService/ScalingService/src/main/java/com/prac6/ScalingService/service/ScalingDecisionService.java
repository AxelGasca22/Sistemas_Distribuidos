package com.prac6.ScalingService.service;

import com.prac6.ScalingService.dto.ScalingRequest;
import com.prac6.ScalingService.dto.ScalingResponse;
import org.springframework.stereotype.Service;

@Service
public class ScalingDecisionService {

    public ScalingResponse evaluate(ScalingRequest request) {
        String action;
        String message;

        if ("CRITICAL".equalsIgnoreCase(request.getState())) {
            action = "SCALE_UP";
            message = "Carga alta detectada para " + request.getType();
        } else if ("LOW".equalsIgnoreCase(request.getState())) {
            action = "SCALE_DOWN";
            message = "Carga baja detectada para " + request.getType();
        } else {
            action = "NO_ACTION";
            message = "No se requiere escalamiento";
        }

        return new ScalingResponse(
                request.getType(),
                request.getState(),
                request.getValue(),
                action,
                message
        );
    }
}