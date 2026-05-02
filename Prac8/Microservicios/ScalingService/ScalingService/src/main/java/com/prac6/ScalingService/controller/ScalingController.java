package com.prac6.ScalingService.controller;

import com.prac6.ScalingService.dto.ScalingRequest;
import com.prac6.ScalingService.dto.ScalingResponse;
import com.prac6.ScalingService.service.ScalingDecisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scaling")
public class ScalingController {

    private final ScalingDecisionService scalingDecisionService;

    public ScalingController(ScalingDecisionService scalingDecisionService) {
        this.scalingDecisionService = scalingDecisionService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ScalingResponse> evaluate(@RequestBody ScalingRequest request) {
        return ResponseEntity.ok(scalingDecisionService.evaluate(request));
    }
}