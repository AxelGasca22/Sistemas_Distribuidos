package com.prac6.ClientSimulator.model;

public class SimulatedNode {

    private String nodeId;
    private String type;
    private String role;

    public SimulatedNode() {
    }

    public SimulatedNode(String nodeId, String type, String role) {
        this.nodeId = nodeId;
        this.type = type;
        this.role = role;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
