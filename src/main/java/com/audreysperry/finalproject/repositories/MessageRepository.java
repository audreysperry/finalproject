package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.Message;
import com.audreysperry.finalproject.models.Thread;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findAllByThread(Thread thread);
    List<Message> findAllByThreadOrderByDateAsc(Thread thread);
}
