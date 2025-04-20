package com.example.projectlabexam;

import java.time.LocalDate;

public class Booking {
    private String room;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Booking(String room, LocalDate checkIn, LocalDate checkOut) {
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return now.isEqual(checkIn) || (now.isAfter(checkIn) && now.isBefore(checkOut));
    }

    public String getRoom() {
        return room;
    }
}