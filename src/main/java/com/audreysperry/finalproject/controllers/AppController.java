package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.googleApi.GeoCodingInterface;
import com.audreysperry.finalproject.googleApi.GeoCodingResponse;
import com.audreysperry.finalproject.models.*;
import com.audreysperry.finalproject.repositories.HostLocationRepository;
import com.audreysperry.finalproject.repositories.RoleRepository;
import com.audreysperry.finalproject.repositories.SpaceRepository;
import com.audreysperry.finalproject.repositories.UserRepository;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private HostLocationRepository locationRepo;

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private RoleRepository roleRepo;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String homePage() {

        return "home";
    }

    @RequestMapping(value="/host", method = RequestMethod.GET)
    public String becomeHost(Model model) {
        model.addAttribute("hostLocation", new HostLocation());

        return "addLocation";
    }

    @RequestMapping(value="/addLocation", method = RequestMethod.POST)
    public String addLocation(@ModelAttribute HostLocation hostLocation,
                              Principal principal,
                              Model model) {
        User currentUser = userRepo.findByUsername(principal.getName());
        Role hostRole = roleRepo.findByName("ROLE_HOST");
        hostLocation.setUser(currentUser);
        currentUser.setRole(hostRole);
        String locationString = hostLocation.getStreetAddress() + ", " + hostLocation.getCity() + ", " + hostLocation.getState() + ", " + hostLocation.getZipCode();

        ApiKey apiKey = new ApiKey();
        GeoCodingInterface geoCodingInterface = Feign.builder()
                                                    .decoder(new GsonDecoder())
                                                    .target(GeoCodingInterface.class, "https://maps.googleapis.com");

        GeoCodingResponse response = geoCodingInterface.geoCodingResponse(locationString, apiKey.getAPI_Key());

        double lat = response.getResults().get(0).getGeometry().getLocation().getLat();
        double lng = response.getResults().get(0).getGeometry().getLocation().getLng();
        hostLocation.setLatitude(lat);
        hostLocation.setLongitude(lng);
        locationRepo.save(hostLocation);
        model.addAttribute("location_id", hostLocation.getId());
        model.addAttribute("space", new Space());
        return "addSpace";

    }

    @RequestMapping(value="/addSpace", method = RequestMethod.POST)
    public String addSpace(@ModelAttribute Space space,
                           @RequestParam("location_id") long location_id) {
        HostLocation currentHostLocation = locationRepo.findOne(location_id);
        space.setHostLocation(currentHostLocation);
        spaceRepo.save(space);
        return "redirect:/";
    }

    @RequestMapping(value="/locationSearch", method = RequestMethod.GET)
    public String locationSearchPage() {
        return "locationSearch";
    }

    @RequestMapping(value="/animalSearch", method = RequestMethod.GET)
    public String animalSearchPage() {
        return "animalSearch";
    }

    @RequestMapping(value="/location/{animalType}", method=RequestMethod.GET)
    public String displaySpaceDetails(
            @PathVariable ("animalType") String animalType,
                                      Model model
    ) {
        System.out.println(animalType);
        model.addAttribute("spaces", spaceRepo.findAllByAnimalType(animalType));
        return "spaceOptions";
    }

    @RequestMapping(value="/editHost", method = RequestMethod.GET)
    public String viewHostInfo(Principal principal,
                               Model model) {
        User currentUser = userRepo.findByUsername(principal.getName());
        HostLocation location = locationRepo.findByUser(currentUser);
        model.addAttribute("location", location);
        model.addAttribute("user", currentUser);

        return "viewHostInfo";
    }

    @RequestMapping(value="/location/{locationid}/edit", method = RequestMethod.GET)
    public String editLocationInfo(Principal principal,
                                   Model model,
                                   @PathVariable("locationid") long locationid) {
        HostLocation currentlocation = locationRepo.findOne(locationid);

        return "locationEditForm";
    }
}
