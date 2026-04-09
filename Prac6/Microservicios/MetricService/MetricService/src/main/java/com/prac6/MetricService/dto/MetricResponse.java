package com.prac6.MetricService.dto;

public class MetricResponse {

    private String nodeId;
    private String type;
    private int value;
    private String status;
    private String scalingAction;
    private String scalingMessage;

    public MetricResponse() {
    }

    public MetricResponse(String nodeId, String type, int value, String status) {
        this.nodeId = nodeId;
        this.type = type;
        this.value = value;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScalingAction() {
        return scalingAction;
    }
    public void setScalingAction(String scalingAction) {
        this.scalingAction = scalingAction;
    }
    public String getScalingMessage() {
        return scalingMessage;
    }
    public void setScalingMessage(String scalingMessage) {
        this.scalingMessage = scalingMessage;
    }
}