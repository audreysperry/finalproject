package com.audreysperry.finalproject.repositories;


import com.audreysperry.finalproject.models.BookingRequest;

import com.audreysperry.finalproject.models.Space;
import com.audreysperry.finalproject.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRequestRepository extends CrudRepository<BookingRequest, Long> {
List<BookingRequest> findAllByHost(User host);

List<BookingRequest> findAllByGuest(User guest);

List<BookingRequest> findAllBySpace(Space space);
}
