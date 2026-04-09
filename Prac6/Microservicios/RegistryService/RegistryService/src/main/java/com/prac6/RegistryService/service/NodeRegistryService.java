package com.prac6.RegistryService.service;

import com.prac6.RegistryService.dto.RegisterRequest;
import com.prac6.RegistryService.model.NodeInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeRegistryService {

    private final Map<String, NodeInfo> nodes = new ConcurrentHashMap<>();

    public NodeInfo registerNode(RegisterRequest request) {
        NodeInfo node = new NodeInfo(
                request.getNodeId(),
                request.getType(),
                request.getRole(),
                System.currentTimeMillis(),
                "ALIVE"
        );
        nodes.put(request.getNodeId(), node);
        return node;
    }

    public NodeInfo heartbeat(String nodeId) {
        NodeInfo node = nodes.get(nodeId);
        if (node != null) {
            node.setLastHeartbeat(System.currentTimeMillis());
            node.setStatus("ALIVE");
        }
        return node;
    }

    public NodeInfo updateStatus(String nodeId, String status) {
        NodeInfo node = nodes.get(nodeId);
        if (node != null) {
            node.setStatus(status);
        }
        return node;
    }

    public NodeInfo getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public Collection<NodeInfo> getAllNodes() {
        return nodes.values();
    }
}