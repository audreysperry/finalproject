package com.audreysperry.finalproject.models;


import javax.persistence.*;

@Entity
@Table(name="booking_requests")
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="num_animals")
    private int numAnimals;


    private User guest;

    private Space space;

    @Column(name="host_response")
    private boolean hostResponse;


}
