package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.models.BookingRequest;
import com.audreysperry.finalproject.models.Space;
import com.audreysperry.finalproject.models.User;
import com.audreysperry.finalproject.repositories.BookingRequestRepository;
import com.audreysperry.finalproject.repositories.SpaceRepository;
import com.audreysperry.finalproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BookSpaceController {

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private BookingRequestRepository bookingRequestRepo;

    @Autowired
    private UserRepository userRepo;

    @RequestMapping(value="/requestSpace/{spaceid}")
    public String requestSpacePage(Model model,
                                Principal principal,
                                @PathVariable("spaceid") long spaceid) {

        Space space = spaceRepo.findOne(spaceid);
        model.addAttribute("space", space);
        model.addAttribute("user", new User());
        model.addAttribute("booking", new BookingRequest());
        return "bookspace/bookSpace";

    }

    @RequestMapping(value="/requestSpace/{spaceid}", method = RequestMethod.POST)
    public String requestSpace(Model model,
                               Principal principal,
                               @ModelAttribute BookingRequest booking,
                               @PathVariable("spaceid") long spaceid) {
        Space space = spaceRepo.findOne(spaceid);
        User host = space.getHostLocation().getUser();
        User guest = userRepo.findByUsername(principal.getName());
        booking.setGuest(guest);
        booking.setHost(host);
        booking.setSpace(space);
        booking.setHostResponse(null);
        bookingRequestRepo.save(booking);

        return "bookspace/confirmScreen";
    }

    @RequestMapping(value="/requests", method = RequestMethod.GET)
    public String displayRequests(Model model,
                                  Principal principal) {
        User host = userRepo.findByUsername(principal.getName());
        List<BookingRequest> tempRequests = bookingRequestRepo.findAllByHost(host);
        List<BookingRequest> openRequests = new ArrayList<BookingRequest>(){};
        for (BookingRequest bookingreq : tempRequests
             ) {
            if (bookingreq.isHostResponse() == null && bookingreq.getHost() == host) {
                openRequests.add(bookingreq);
            }

        }
        model.addAttribute("bookingreqs", openRequests);
        return "bookspace/requests";
    }
}
