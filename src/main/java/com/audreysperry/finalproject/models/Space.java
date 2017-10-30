package com.audreysperry.finalproject.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="spaces")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="animal_type")
    private String animalType;

    @Column(name="animal_number")
    private int animalNumber;

    private String notes;

    @Column(name="image_path")
    private String imagePath;

    private boolean active;


    @ManyToOne
    @JoinColumn(name="location_id")
    private HostLocation hostLocation;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<BookingRequest> bookingRequests;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public int getAnimalNumber() {
        return animalNumber;
    }

    public void setAnimalNumber(int animalNumber) {
        this.animalNumber = animalNumber;
    }

    public HostLocation getHostLocation() {
        return hostLocation;
    }

    public void setHostLocation(HostLocation hostLocation) {
        this.hostLocation = hostLocation;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<BookingRequest> getBookingRequests() {
        return bookingRequests;
    }

    public void setBookingRequests(List<BookingRequest> bookingRequests) {
        this.bookingRequests = bookingRequests;
    }

    public List<Long> getRequesters () {
        List<BookingRequest> bookingrequests = this.bookingRequests;
        List<Long> requestors = new ArrayList<>();
        for (BookingRequest bookingrequest : bookingrequests
             ) {
            Long userid = bookingrequest.getGuest().getId();
            requestors.add(userid);

        }

        return requestors;
    }
}
