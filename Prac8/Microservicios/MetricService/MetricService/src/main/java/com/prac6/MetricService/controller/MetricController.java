package com.prac6.MetricService.controller;

import com.prac6.MetricService.dto.MetricRequest;
import com.prac6.MetricService.dto.MetricResponse;
import com.prac6.MetricService.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.prac6.MetricService.dto.MetricRequest;

@RestController
@RequestMapping("/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping("/report")
    public ResponseEntity<MetricResponse> report(@RequestBody MetricRequest request) {
        MetricResponse response = metricService.processMetric(request);
        return ResponseEntity.ok(response);
    }
}