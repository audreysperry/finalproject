package com.audreysperry.finalproject.repositories;

import com.audreysperry.finalproject.models.Location;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends CrudRepository<Location, Long> {
}
