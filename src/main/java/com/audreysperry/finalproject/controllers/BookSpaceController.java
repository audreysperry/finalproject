package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.models.*;
import com.audreysperry.finalproject.models.Thread;
import com.audreysperry.finalproject.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class BookSpaceController {

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private BookingRequestRepository bookingRequestRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ThreadRepository threadRepo;

    @Autowired
    private MessageRepository messageRepo;

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

    @RequestMapping(value="/acceptRequest/{reqid}", method = RequestMethod.POST)
    public String acceptRequest(Model model,
                                @PathVariable("reqid") long reqid,
                                Principal principal) {
        BookingRequest request = bookingRequestRepo.findOne(reqid);
        User guest = request.getGuest();
        request.setHostResponse(true);
        int reqNumber = request.getNumAnimals();
        Space space = request.getSpace();
        int spaceNumber = space.getAnimalNumber();
        int newAvailability = spaceNumber - reqNumber;
        if (newAvailability <= 0) {
            space.setActive(false);
        }
        space.setAnimalNumber(newAvailability);

        // add current open requests to model to display on screen
        User host = userRepo.findByUsername(principal.getName());
        List<BookingRequest> tempRequests = bookingRequestRepo.findAllByHost(host);
        List<BookingRequest> openRequests = new ArrayList<BookingRequest>(){};
        for (BookingRequest bookingreq : tempRequests
                ) {
            if (bookingreq.isHostResponse() == null && bookingreq.getHost() == host) {
                openRequests.add(bookingreq);
            }
        }

        // create new thread and send guest message of approval
        Thread thread = new Thread();
        thread.setHostName(host.getUsername());
        thread.setHost(host);
        thread.setGuestName(guest.getUsername());
        thread.setGuest(guest);
        threadRepo.save(thread);
        Message message = new Message();
        String noteForGuest = host.getFirstName() + " " + host.getLastName() +  " accepted your booking request for " + request.getNumAnimals() + " " + space.getAnimalType() + ". The address is " + space.getHostLocation().getStreetAddress() + " " + space.getHostLocation().getCity() + ", " + space.getHostLocation().getState() + " " + space.getHostLocation().getZipCode();
        message.setNote(noteForGuest);

        message.setThread(thread);
        message.setAuthorUsername(host.getUsername());
        message.setRecipient(guest.getUsername());
        message.setDate(new Date());
        message.setReceiver(guest);
        message.setSender(host);
        messageRepo.save(message);

        spaceRepo.save(space);
        bookingRequestRepo.save(request);
        model.addAttribute("bookingreqs", openRequests);
        return "bookspace/requests";
    }

    @RequestMapping(value="/denyRequest/{reqid}", method = RequestMethod.POST)
    public String denyRequest(Model model,
                              Principal principal,
                              @PathVariable("reqid") long reqid) {

        BookingRequest request = bookingRequestRepo.findOne(reqid);
        User guest = request.getGuest();
        Space space = request.getSpace();
        request.setHostResponse(false);

        // add current open requests to model to display on screen
        User host = userRepo.findByUsername(principal.getName());
        List<BookingRequest> tempRequests = bookingRequestRepo.findAllByHost(host);
        List<BookingRequest> openRequests = new ArrayList<BookingRequest>(){};
        for (BookingRequest bookingreq : tempRequests
                ) {
            if (bookingreq.isHostResponse() == null && bookingreq.getHost() == host) {
                openRequests.add(bookingreq);
            }
        }

        // create new thread and send guest message of approval
        Thread thread = new Thread();
        thread.setHostName(host.getUsername());
        thread.setHost(host);
        thread.setGuestName(guest.getUsername());
        thread.setGuest(guest);
        threadRepo.save(thread);
        Message message = new Message();
        String noteForGuest = host.getFirstName() + " " + host.getLastName() +  " was unable to accept your booking request for " + request.getNumAnimals() + " " + space.getAnimalType() + ".";
        message.setNote(noteForGuest);

        message.setThread(thread);
        message.setAuthorUsername(host.getUsername());
        message.setRecipient(guest.getUsername());
        message.setDate(new Date());
        message.setReceiver(guest);
        message.setSender(host);
        messageRepo.save(message);

        spaceRepo.save(space);
        bookingRequestRepo.save(request);
        model.addAttribute("bookingreqs", openRequests);
        return "bookspace/requests";

    }

    @RequestMapping(value="/acceptReqCloseSpace/{reqid}", method = RequestMethod.POST)
    public String acceptRequestCloseSpace(Model model,
                                          Principal principal,
                                          @PathVariable("reqid") long reqid) {

        BookingRequest request = bookingRequestRepo.findOne(reqid);
        User host = userRepo.findByUsername(principal.getName());
        User guest = request.getGuest();
        request.setHostResponse(true);
        int reqNumber = request.getNumAnimals();
        Space space = request.getSpace();
        space.setActive(false);
        space.setAnimalNumber(0);

        // create new thread and send guest message of approval
        Thread thread = new Thread();
        thread.setHostName(host.getUsername());
        thread.setHost(host);
        thread.setGuestName(guest.getUsername());
        thread.setGuest(guest);
        threadRepo.save(thread);
        Message message = new Message();
        String noteForGuest = host.getFirstName() + " " + host.getLastName() +  " accepted your booking request for " + request.getNumAnimals() + " " + space.getAnimalType() + ". The address is " + space.getHostLocation().getStreetAddress() + " " + space.getHostLocation().getCity() + ", " + space.getHostLocation().getState() + " " + space.getHostLocation().getZipCode();
        message.setNote(noteForGuest);

        message.setThread(thread);
        message.setAuthorUsername(host.getUsername());
        message.setRecipient(guest.getUsername());
        message.setDate(new Date());
        message.setReceiver(guest);
        message.setSender(host);
        messageRepo.save(message);

        // add current open requests to model to display on screen
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
