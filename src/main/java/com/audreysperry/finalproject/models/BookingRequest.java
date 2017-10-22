package com.audreysperry.finalproject.models;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="booking_requests")
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="num_animals")
    private int numAnimals;

    @ManyToOne
    @JoinColumn(name="guest_id")
    private User guest;

    @ManyToOne
    @JoinColumn(name="space_id")
    private Space space;

    @ManyToOne
    @JoinColumn(name="host_id")
    private User host;

    @Column(name="host_response")
    private Boolean hostResponse;

    private String notes;

    @OneToOne
    @JoinColumn(name="thread_id")
    private Thread thread;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumAnimals() {
        return numAnimals;
    }

    public void setNumAnimals(int numAnimals) {
        this.numAnimals = numAnimals;
    }

    public User getGuest() {
        return guest;
    }

    public void setGuest(User guest) {
        this.guest = guest;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public Boolean isHostResponse() {
        return hostResponse;
    }

    public void setHostResponse(Boolean hostResponse) {
        this.hostResponse = hostResponse;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
