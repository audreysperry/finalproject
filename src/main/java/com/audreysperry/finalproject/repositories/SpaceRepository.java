package com.audreysperry.finalproject.repositories;


import com.audreysperry.finalproject.models.Space;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository extends CrudRepository<Space, Long> {
    List<Space> findAllByAnimalType(String animalType);
    List<Space> findAllByAnimalTypeAndHostLocation_State(String animalType, String state);
    List<Space> findAllByAnimalTypeAndHostLocationState(String animalType, String state);
    List<Space> findAllByHostLocation_State(String state);
}
