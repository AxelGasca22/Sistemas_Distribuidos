package com.prac6.FailureService.dto;

public class NodeInfo {
    private String nodeId;
    private String type;
    private String role;
    private long lastHeartbeat;
    private String status;

    public NodeInfo() {}

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
