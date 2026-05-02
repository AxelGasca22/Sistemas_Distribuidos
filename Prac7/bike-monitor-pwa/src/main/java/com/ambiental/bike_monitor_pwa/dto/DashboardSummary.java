package com.ambiental.bike_monitor_pwa.dto;

public class DashboardSummary {

    private int totalStations;
    private int availableStations;
    private int lowStations;
    private int emptyStations;
    private int offlineStations;
    private int totalAvailableBikes;

    public DashboardSummary() {
    }

    public DashboardSummary(int totalStations, int availableStations, int lowStations, int emptyStations, int offlineStations, int totalAvailableBikes) {
        this.totalStations = totalStations;
        this.availableStations = availableStations;
        this.lowStations = lowStations;
        this.emptyStations = emptyStations;
        this.offlineStations = offlineStations;
        this.totalAvailableBikes = totalAvailableBikes;
    }

    public int getTotalStations() {
        return totalStations;
    }

    public void setTotalStations(int totalStations) {
        this.totalStations = totalStations;
    }

    public int getAvailableStations() {
        return availableStations;
    }

    public void setAvailableStations(int availableStations) {
        this.availableStations = availableStations;
    }

    public int getLowStations() {
        return lowStations;
    }

    public void setLowStations(int lowStations) {
        this.lowStations = lowStations;
    }

    public int getEmptyStations() {
        return emptyStations;
    }

    public void setEmptyStations(int emptyStations) {
        this.emptyStations = emptyStations;
    }

    public int getOfflineStations() {
        return offlineStations;
    }

    public void setOfflineStations(int offlineStations) {
        this.offlineStations = offlineStations;
    }

    public int getTotalAvailableBikes() {
        return totalAvailableBikes;
    }

    public void setTotalAvailableBikes(int totalAvailableBikes) {
        this.totalAvailableBikes = totalAvailableBikes;
    }
}
