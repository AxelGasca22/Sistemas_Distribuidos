package com.prac6.MetricService.dto;

public class ScalingResponse {
    private String type;
    private String state;
    private int value;
    private String action;
    private String message;

    public ScalingResponse() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
