package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
}
