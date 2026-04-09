package com.prac6.RegistryService.dto;

public class UpdateStatusRequest {
    private String nodeId;
    private String status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String nodeId, String status) {
        this.nodeId = nodeId;
        this.status = status;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
