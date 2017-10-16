package com.audreysperry.finalproject.models;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="threads")
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL)
    private List<Message> messages;
}
