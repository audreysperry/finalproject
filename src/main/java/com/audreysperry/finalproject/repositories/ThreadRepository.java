package com.audreysperry.finalproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.audreysperry.finalproject.models.Thread;

import java.util.List;


@Repository
public interface ThreadRepository extends CrudRepository<Thread, Long>{
    List<Thread> findAllByHostName(String hostName);
    List<Thread> findAllByGuestName(String guestName);
}
