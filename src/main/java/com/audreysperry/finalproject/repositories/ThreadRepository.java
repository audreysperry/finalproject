package com.audreysperry.finalproject.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.audreysperry.finalproject.models.Thread;


@Repository
public interface ThreadRepository extends CrudRepository<Thread, Long>{
}
