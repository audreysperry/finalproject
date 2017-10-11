package com.audreysperry.finalproject.models;


import javax.persistence.*;

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

    @ManyToOne
    @JoinColumn(name="location_id")
    private HostLocation hostLocation;

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
}
