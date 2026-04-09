package com.prac6.MetricService.dto;

public class ScalingRequest {
    private String type;
    private String state;
    private int value;

    public ScalingRequest() {}

    public ScalingRequest(String type, String state, int value) {
        this.type = type;
        this.state = state;
        this.value = value;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
