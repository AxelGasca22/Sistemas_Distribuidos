package com.prac6.MetricService.client;

import com.prac6.MetricService.dto.ScalingRequest;
import com.prac6.MetricService.dto.ScalingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "scalingservice")
public interface ScalingClient {

    @PostMapping("/scaling/evaluate")
    ScalingResponse evaluate(@RequestBody ScalingRequest request);
}
