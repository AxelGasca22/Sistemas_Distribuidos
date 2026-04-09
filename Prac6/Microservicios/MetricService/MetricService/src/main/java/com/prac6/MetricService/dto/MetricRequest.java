package com.prac6.MetricService.dto;

public class MetricRequest {

    private String nodeId;
    private String type;
    private int value;

    public MetricRequest() {
    }

    public MetricRequest(String nodeId, String type, int value) {
        this.nodeId = nodeId;
        this.type = type;
        this.value = value;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}