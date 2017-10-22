package com.audreysperry.finalproject.models;


import javax.persistence.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import com.audreysperry.finalproject.models.User;


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

    @ManyToOne
    @JoinColumn(name="host_id")
    private User host;

    @ManyToOne
    @JoinColumn(name="guest_id")
    private User guest;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<Message> messages;

    @OneToOne(mappedBy = "thread", cascade = CascadeType.ALL)
    private BookingRequest bookingRequest;

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

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public User getGuest() {
        return guest;
    }

    public void setGuest(User guest) {
        this.guest = guest;
    }

    public int getUnreadMessages(org.springframework.security.core.userdetails.User user) {
        List<Message> unreadMessages = new ArrayList<Message>() {};
        List<Message> allMessages = getMessages();
        for (Message message : allMessages
                ) {
            if (message.isMessageRead() == false && user.getUsername().equals(message.getReceiver().getUsername())) {
                unreadMessages.add(message);
            }
        }
        return unreadMessages.size();
    }

}
