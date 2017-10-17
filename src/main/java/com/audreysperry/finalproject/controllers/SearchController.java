package com.audreysperry.finalproject.controllers;

import com.audreysperry.finalproject.googleApi.GeoCodingInterface;
import com.audreysperry.finalproject.googleApi.GeoCodingResponse;
import com.audreysperry.finalproject.models.ApiKey;
import com.audreysperry.finalproject.models.HostLocation;
import com.audreysperry.finalproject.models.Space;
import com.audreysperry.finalproject.repositories.*;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private HostLocationRepository locationRepo;

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ThreadRepository threadRepo;


    @RequestMapping(value="/locationSearch", method = RequestMethod.GET)
    public String locationSearchPage() {
        return "locationSearch";
    }

    @RequestMapping(value="/locationSearchResults", method = RequestMethod.GET)
    public String showLocationSearchResults(Model model,
                                            @RequestParam("city") String city,
                                            @RequestParam("state") String state) {
        String location = city + ", " + state;

        ApiKey apiKey = new ApiKey();
        GeoCodingInterface geoCodingInterface = Feign.builder()
                .decoder(new GsonDecoder())
                .target(GeoCodingInterface.class, "https://maps.googleapis.com");

        GeoCodingResponse response = geoCodingInterface.geoCodingResponse(location, apiKey.getAPI_Key());

        double lat = response.getResults().get(0).getGeometry().getLocation().getLat();
        double lng = response.getResults().get(0).getGeometry().getLocation().getLng();

        double latMax = lat + 1;
        double latMin = lat -1;

        double lngMax = lng + 1;
        double lngMin = lng -1;

        List<HostLocation> hostLocations = new ArrayList<HostLocation>() {
        };

        List<HostLocation> tempLocations = locationRepo.findAll();

        for (HostLocation tempLocation : tempLocations) {
            double tempLat = tempLocation.getLatitude();
            double tempLng = tempLocation.getLongitude();
            if(tempLat >= latMin && tempLat <= latMax && tempLng >= lngMin && tempLng <= lngMax) {
                hostLocations.add(tempLocation);
            }
        }
        model.addAttribute("locations", hostLocations);
        return "locationSearch";
    }

    @RequestMapping(value="/shelterSearch", method = RequestMethod.GET)
    public String shelterSearchPage() {
        return "shelterSearch";
    }

    @RequestMapping(value="/search/{shelterType}", method = RequestMethod.GET)
    public String displayShelterSearch(Model model,
                                       Principal principal,
                                       @PathVariable("shelterType") String shelterType) {
        List<HostLocation> hostLocations = locationRepo.findAllByType(shelterType);
        model.addAttribute("type", shelterType);
        model.addAttribute("locations", hostLocations);

        return "shelterOptions";

    }

    @RequestMapping(value="/animalSearch", method = RequestMethod.GET)
    public String animalSearchPage() {
        return "animalSearch";
    }

    @RequestMapping(value="/location/{animalType}", method=RequestMethod.GET)
    public String displaySpaceDetails(
            @PathVariable ("animalType") String animalType,
            Model model) {
        model.addAttribute("spaces", spaceRepo.findAllByAnimalType(animalType));
        model.addAttribute("animalType", animalType);
        return "spaceOptions";
    }

    @RequestMapping(value="/stateResults", method = RequestMethod.GET)
    public String filterByState(Model model,
                                @RequestParam("state") String state,
                                @RequestParam("animalType") String animalType) {
        System.out.println(state);
        List<Space> spaces = spaceRepo.findAllByAnimalTypeAndHostLocation_State(animalType, state);
        System.out.println("these are spaces: " + spaces);
        model.addAttribute("spaces", spaces);
        model.addAttribute("animalType", animalType);
        return "spaceOptions";
    }
}
