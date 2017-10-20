package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.models.BookingRequest;
import com.audreysperry.finalproject.models.Space;
import com.audreysperry.finalproject.models.User;
import com.audreysperry.finalproject.repositories.BookingRequestRepository;
import com.audreysperry.finalproject.repositories.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class BookSpaceController {

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private BookingRequestRepository bookingRequestRepo;

    @RequestMapping(value="/bookSpace/{spaceid}")
    public String bookSpacePage(Model model,
                                Principal principal,
                                @PathVariable("spaceid") long spaceid) {

        Space space = spaceRepo.findOne(spaceid);
        model.addAttribute("space", space);
        model.addAttribute("user", new User());
        model.addAttribute("booking", new BookingRequest());
        return "bookspace/bookSpace";

    }
}
