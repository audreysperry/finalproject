package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.HostLocation;
import com.audreysperry.finalproject.models.User;
import org.apache.catalina.Host;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostLocationRepository extends CrudRepository<HostLocation, Long> {
    HostLocation findByUser(User user);

    List<HostLocation> findAllByType(String shelterType);

    List<HostLocation> findAllByTypeAndState(String type, String state);

    List<HostLocation> findAll();

    HostLocation findById(long id);

}
