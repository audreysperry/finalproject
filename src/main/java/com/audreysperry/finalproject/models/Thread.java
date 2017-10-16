package com.audreysperry.finalproject.models;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="threads")
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "host_name")
    private String hostName;

    @Column(name="guest_name")
    private String guestName;


    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<Message> messages;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
}
