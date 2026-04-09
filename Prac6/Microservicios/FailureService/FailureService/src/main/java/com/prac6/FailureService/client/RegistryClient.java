package com.prac6.FailureService.client;

import com.prac6.FailureService.dto.NodeInfo;
import com.prac6.FailureService.dto.UpdateStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;

@FeignClient(name = "registryservice")
public interface RegistryClient {

    @GetMapping("/registry/nodes")
    Collection<NodeInfo> getNodes();

    @PostMapping("/registry/status")
    Object updateStatus(@RequestBody UpdateStatusRequest request);
}
