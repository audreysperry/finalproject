package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.HostLocation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostLocationRepository extends CrudRepository<HostLocation, Long> {
}
