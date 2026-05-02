package com.prac6.RegistryService.dto;

public class HeartbeatRequest {
    private String nodeId;

    public HeartbeatRequest() {
    }

    public HeartbeatRequest(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
