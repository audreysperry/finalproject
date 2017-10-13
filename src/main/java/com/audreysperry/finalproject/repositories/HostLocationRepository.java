package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.HostLocation;
import com.audreysperry.finalproject.models.User;
import org.apache.catalina.Host;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostLocationRepository extends CrudRepository<HostLocation, Long> {
    HostLocation findByUser(User user);
}
