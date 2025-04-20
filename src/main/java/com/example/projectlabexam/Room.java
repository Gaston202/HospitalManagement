package com.example.projectlabexam;

public class Room {
    private final String roomNumber;
    private final String roomType;
    private String status;
    private String lastCleaned;

    public Room(String roomNumber, String roomType, String status, String lastCleaned) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.status = status;
        this.lastCleaned = lastCleaned;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public String getStatus() { return status; }
    public String getLastCleaned() { return lastCleaned; }

    public void setStatus(String status) { this.status = status; }
    public void setLastCleaned(String lastCleaned) { this.lastCleaned = lastCleaned; }
}
