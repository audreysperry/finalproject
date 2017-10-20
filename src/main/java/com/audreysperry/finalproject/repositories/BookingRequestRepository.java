package com.audreysperry.finalproject.repositories;


import com.audreysperry.finalproject.models.BookingRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRequestRepository extends CrudRepository<BookingRequest, Long> {

}
