package com.audreysperry.finalproject.repositories;


import com.audreysperry.finalproject.models.Space;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends CrudRepository<Space, Long> {
}
