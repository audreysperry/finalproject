package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.SMSHelper;
import com.audreysperry.finalproject.models.*;
import com.audreysperry.finalproject.models.Thread;
import com.audreysperry.finalproject.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class BookSpaceController {




    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private HostLocationRepository locationRepo;

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
        Boolean hasAlreadyBooked = false;
        User currentUser = userRepo.findByUsername(principal.getName());
        List<BookingRequest> bookingRequests = (List<BookingRequest>) bookingRequestRepo.findAll();
        Space space = spaceRepo.findOne(spaceid);

        for (BookingRequest bookingRequest : bookingRequests
             ) {
            if (bookingRequest.getGuest() == currentUser && bookingRequest.getSpace() == space) {
                hasAlreadyBooked = true;
            }

        }
        model.addAttribute("requestSent", hasAlreadyBooked);
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
        Thread thread = new Thread();
        thread.setGuest(guest);
        thread.setHost(host);
        thread.setHostName(host.getUsername());
        thread.setGuestName(guest.getUsername());
        threadRepo.save(thread);
        booking.setThread(thread);
        booking.setGuest(guest);
        booking.setHost(host);
        booking.setSpace(space);
        booking.setHostResponse(null);
        bookingRequestRepo.save(booking);

        return "bookspace/confirmScreen";
    }

    @RequestMapping(value="/hostRequests", method = RequestMethod.GET)
    public String displayHostRequests(Model model,
                                  Principal principal) {
        // Requests if logged in user is host.
        User host = userRepo.findByUsername(principal.getName());



        HostLocation hostLocation = locationRepo.findByUser(host);
            List<Space> spaces = hostLocation.getSpaces();
            List<BookingRequest> tempRequests = bookingRequestRepo.findAllByHost(host);
            List<BookingRequest> openRequests = new ArrayList<BookingRequest>() {
            };
            for (BookingRequest bookingreq : tempRequests
                    ) {
                if (bookingreq.isHostResponse() == null && bookingreq.getHost() == host) {
                    openRequests.add(bookingreq);
                }

            }

        model.addAttribute("spaces", spaces);
        model.addAttribute("bookingreqs", openRequests);
        return "bookspace/hostRequests";
    }

    @RequestMapping(value="guestRequests", method = RequestMethod.GET)
    public String displayGuestRequests(Model model,
                                       Principal principal) {

        User guest = userRepo.findByUsername(principal.getName());

        List<BookingRequest> tempGuestRequests = bookingRequestRepo.findAllByGuest(guest);

        List<BookingRequest> pendingRequests = new ArrayList<BookingRequest>() {
        };
        List<BookingRequest> acceptedRequests = new ArrayList<BookingRequest>() {
        };
        List<BookingRequest> deniedRequests = new ArrayList<BookingRequest>() {
        };


            for (BookingRequest bookReq : tempGuestRequests
                    ) {
                if (bookReq.isHostResponse() == null && bookReq.getGuest() == guest) {
                    pendingRequests.add(bookReq);
                } else if (bookReq.isHostResponse() == true && bookReq.getGuest() == guest) {
                    acceptedRequests.add(bookReq);
                } else if (bookReq.isHostResponse() == false && bookReq.getGuest() == guest) {
                    deniedRequests.add(bookReq);
                }
            }

        model.addAttribute("deniedReqs", deniedRequests);
        model.addAttribute("acceptedReqs", acceptedRequests);
        model.addAttribute("pendingReqs", pendingRequests);

        return "bookspace/guestRequests";
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
            List<BookingRequest> openRequests = bookingRequestRepo.findAllBySpace(space);
            for (BookingRequest openReq : openRequests
                    ) { openReq.setHostResponse(false);
                bookingRequestRepo.save(openReq);
            }
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

        // find thread and send guest message of approval
        Thread thread = request.getThread();
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

        if (!request.getGuestPhone().isEmpty()) {

            String guestNumber = "+1" + request.getGuestPhone();
            SMSHelper.sendMessage(guestNumber, noteForGuest);
        }

        spaceRepo.save(space);
        bookingRequestRepo.save(request);
        model.addAttribute("bookingreqs", openRequests);
        return "redirect:/hostRequests";
    }

    @RequestMapping(value="/denyRequest/{reqid}", method = RequestMethod.POST)
    public String denyRequest(Model model,
                              Principal principal,
                              @PathVariable("reqid") long reqid) {

        BookingRequest request = bookingRequestRepo.findOne(reqid);
        User guest = request.getGuest();
        User host = userRepo.findByUsername(principal.getName());
        Space space = request.getSpace();
        request.setHostResponse(false);
        Thread thread = request.getThread();



        spaceRepo.save(space);
        bookingRequestRepo.save(request);

        if (!request.getGuestPhone().isEmpty()) {

            String noteForGuest = "Sorry. " + request.getHost().getFirstName() + " in " + space.getHostLocation().getCity() + ", " + space.getHostLocation().getState() + " was unable to accept your booking request for " + request.getNumAnimals() + " " + space.getAnimalType() + ".";
            String guestNumber = "+1" + request.getGuestPhone();
            SMSHelper.sendMessage(guestNumber, noteForGuest);
        }

        return "redirect:/hostRequests";

    }

    @RequestMapping(value="/acceptReqCloseSpace/{reqid}", method = RequestMethod.POST)
    public String acceptRequestCloseSpace(Model model,
                                          Principal principal,
                                          @PathVariable("reqid") long reqid) {

        BookingRequest request = bookingRequestRepo.findOne(reqid);
        User host = userRepo.findByUsername(principal.getName());
        User guest = request.getGuest();
        request.setHostResponse(true);
        bookingRequestRepo.save(request);
        Space space = request.getSpace();
        space.setActive(false);
        space.setAnimalNumber(0);
        spaceRepo.save(space);

        List<BookingRequest> openRequests = bookingRequestRepo.findAllBySpace(space);
        for (BookingRequest openReq : openRequests
                ) {
            if (openReq.isHostResponse() == null) {

                openReq.setHostResponse(false);
                bookingRequestRepo.save(openReq);

                String guestNumber = "+1" + openReq.getGuestPhone();
                String noteForGuest = "Sorry. " + request.getHost().getFirstName() + " in " + space.getHostLocation().getCity() + ", " + space.getHostLocation().getState() + " was unable to accept your booking request for " + request.getNumAnimals() + " " + space.getAnimalType() + ".";
                SMSHelper.sendMessage(guestNumber, noteForGuest);

            }

        }

        //  Send guest message of approval
        Thread thread = request.getThread();
        Message message = new Message();
        String noteForGuest = host.getFirstName() + " " + host.getLastName() +  " accepted your booking request for " + request.getNumAnimals() + " " + space.getAnimalType() + ". The address for the space is " + space.getHostLocation().getStreetAddress() + " " + space.getHostLocation().getCity() + ", " + space.getHostLocation().getState() + " " + space.getHostLocation().getZipCode();
        message.setNote(noteForGuest);

        message.setThread(thread);
        message.setAuthorUsername(host.getUsername());
        message.setRecipient(guest.getUsername());
        message.setDate(new Date());
        message.setReceiver(guest);
        message.setSender(host);
        messageRepo.save(message);

        if (!request.getGuestPhone().isEmpty()) {

            String guestNumber = "+1" + request.getGuestPhone();
            SMSHelper.sendMessage(guestNumber, noteForGuest);

        }

        return "redirect:/hostRequests";
    }

    @RequestMapping(value="/cancelRequest/{reqid}", method = RequestMethod.POST)
    public String cancelRequest(Model model,
                                Principal principal,
                                @PathVariable("reqid") long reqid) {
        BookingRequest request = bookingRequestRepo.findOne(reqid);
        request.setHostResponse(false);
        bookingRequestRepo.save(request);

        return "redirect:/guestRequests";
    }

    @RequestMapping(value="/cancelReservation/{reqid}", method = RequestMethod.POST)
    public String cancelReservation(Model model,
                                    Principal principal,
                                    @PathVariable("reqid") long reqid,
                                    @RequestParam("message") String note) {
        User guest = userRepo.findByUsername(principal.getName());
        BookingRequest bookingReq = bookingRequestRepo.findOne(reqid);
        bookingReq.setHostResponse(false);
        bookingRequestRepo.save(bookingReq);
        Space space = bookingReq.getSpace();
        int numAnimalReq = bookingReq.getNumAnimals();
        int numAnimalSpace = space.getAnimalNumber();
        int newSpaceAvail = numAnimalSpace + numAnimalReq;
        space.setAnimalNumber(newSpaceAvail);
        spaceRepo.save(space);
        User host = bookingReq.getHost();
        Thread thread = bookingReq.getThread();
        Message message = new Message();
        message.setDate(new Date());
        message.setNote(note);
        message.setThread(thread);
        message.setRecipient(host.getUsername());
        message.setReceiver(host);
        message.setSender(guest);
        message.setAuthorUsername(guest.getUsername());
        messageRepo.save(message);

        
        return "redirect:/guestRequests";
    }
}
