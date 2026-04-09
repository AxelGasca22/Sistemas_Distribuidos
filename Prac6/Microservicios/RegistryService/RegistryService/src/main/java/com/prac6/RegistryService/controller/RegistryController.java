package com.prac6.RegistryService.controller;

import com.prac6.RegistryService.dto.HeartbeatRequest;
import com.prac6.RegistryService.dto.RegisterRequest;
import com.prac6.RegistryService.dto.UpdateStatusRequest;
import com.prac6.RegistryService.model.NodeInfo;
import com.prac6.RegistryService.service.NodeRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/registry")
public class RegistryController {

    private final NodeRegistryService service;

    public RegistryController(NodeRegistryService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<NodeInfo> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.registerNode(request));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody HeartbeatRequest request) {
        NodeInfo node = service.heartbeat(request.getNodeId());
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }

    @PostMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody UpdateStatusRequest request) {
        NodeInfo node = service.updateStatus(request.getNodeId(), request.getStatus());
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }

    @GetMapping("/nodes")
    public ResponseEntity<Collection<NodeInfo>> getAllNodes() {
        return ResponseEntity.ok(service.getAllNodes());
    }

    @GetMapping("/nodes/{nodeId}")
    public ResponseEntity<?> getNode(@PathVariable String nodeId) {
        NodeInfo node = service.getNode(nodeId);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }
}
