package com.audreysperry.finalproject.models;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="host_locations")
public class HostLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String type;

    @Column(name="street_address")
    private String streetAddress;

    private String city;

    private String state;

    private double latitude;

    private double longitude;

    @Column(name="zip_code")
    private String zipCode;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "hostLocation", cascade = CascadeType.ALL)
    private List<Space> spaces;


    public List<Space> getActiveSpaces() {
         List<Space> activeSpaces = new ArrayList<Space>();

        for (Space space : spaces
             ) {
            if (space.isActive() == true) {
                activeSpaces.add(space);
            }

        }
        return activeSpaces;
    }

    @Transient
    private String apiKey = System.getenv("GOOGLE_STATIC_MAP_API_KEY");

    public String getStaticMap() {
        String urlString = "https://maps.googleapis.com/maps/api/staticmap?zoom=7&size=400x400&maptype=roadmap&markers=color:blue%7C" + this.latitude + "," + this.longitude + "&key=" + this.apiKey;
        return urlString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }
}
