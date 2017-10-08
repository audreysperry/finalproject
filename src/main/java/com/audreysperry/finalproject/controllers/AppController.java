package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.models.Location;
import com.audreysperry.finalproject.models.User;
import com.audreysperry.finalproject.repositories.LocationRepository;
import com.audreysperry.finalproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private LocationRepository locationRepo;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String homePage() {

        return "home";
    }

    @RequestMapping(value="/host", method = RequestMethod.GET)
    public String becomeHost(Model model) {
        model.addAttribute("location", new Location());

        return "addLocation";
    }

    @RequestMapping(value="addLocation", method = RequestMethod.POST)
    public String addLocation(@ModelAttribute Location location,
                              Principal principal,
                              Model model) {
        User currentUser = userRepo.findByUsername(principal.getName());
        location.setUser(currentUser);
        locationRepo.save(location);
        model.addAttribute("space", new Space());

        return "addAvailability";

    }


}
