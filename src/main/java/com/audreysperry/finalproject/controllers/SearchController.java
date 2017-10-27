package com.audreysperry.finalproject.controllers;

import com.audreysperry.finalproject.googleApi.GeoCodingInterface;
import com.audreysperry.finalproject.googleApi.GeoCodingResponse;
import com.audreysperry.finalproject.models.ApiKey;
import com.audreysperry.finalproject.models.HostLocation;
import com.audreysperry.finalproject.models.Space;
import com.audreysperry.finalproject.models.User;
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
    private HostLocationRepository locationRepo;

    @Autowired
    private SpaceRepository spaceRepo;




    @RequestMapping(value="/locationSearch", method = RequestMethod.GET)
    public String locationSearchPage(Model model) {

        List<HostLocation> hostLocations = locationRepo.findAll();
        String urlString = "https://maps.googleapis.com/maps/api/staticmap?zoom=12&size=400x400&maptype=roadmap&markers=color:green%7C";

        model.addAttribute("urlString", urlString);
        model.addAttribute("apiKey", System.getenv("GOOGLE_STATIC_MAP_API_KEY"));
        model.addAttribute("user", new User());
        model.addAttribute("locations", hostLocations);
        return "searches/locationSearch";
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
        model.addAttribute("user", new User());
        return "searches/locationSearch";
    }

    @RequestMapping(value="/shelterSearch", method = RequestMethod.GET)
    public String shelterSearchPage(Model model) {
        model.addAttribute("user", new User());
        return "searches/shelterSearch";
    }

    @RequestMapping(value="/search/{shelterType}", method = RequestMethod.GET)
    public String displayShelterSearch(Model model,
                                       Principal principal,
                                       @PathVariable("shelterType") String shelterType) {
        List<HostLocation> hostLocations = locationRepo.findAllByType(shelterType);
        model.addAttribute("type", shelterType);
        model.addAttribute("locations", hostLocations);
        model.addAttribute("user", new User());
        return "searches/shelterOptions";

    }

    @RequestMapping(value="/shelterStateResults", method = RequestMethod.GET)
    public String filterShelterOptionsByState(Model model,
                                              Principal principal,
                                              @RequestParam("state") String state,
                                              @RequestParam("shelterType") String shelterType) {
        List<HostLocation> hostLocations = locationRepo.findAllByTypeAndState(shelterType, state);

        model.addAttribute("type", shelterType);
        model.addAttribute("locations", hostLocations);
        model.addAttribute("user", new User());
        return "searches/shelterOptions";
    }

    @RequestMapping(value="/animalSearch", method = RequestMethod.GET)
    public String animalSearchPage(Model model) {
        model.addAttribute("user", new User());
        return "searches/animalSearch";
    }

    @RequestMapping(value="/location/{animalType}", method=RequestMethod.GET)
    public String displaySpaceDetails(
            @PathVariable ("animalType") String animalType,
            Model model) {

        model.addAttribute("spaces", spaceRepo.findAllByAnimalType(animalType));
        model.addAttribute("animalType", animalType);
        model.addAttribute("user", new User());
        return "searches/spaceOptions";
    }

    @RequestMapping(value="/stateResults", method = RequestMethod.GET)
    public String filterByState(Model model,
                                @RequestParam("state") String state,
                                @RequestParam("animalType") String animalType) {
        List<Space> spaces = spaceRepo.findAllByAnimalTypeAndHostLocation_State(animalType, state);
        model.addAttribute("spaces", spaces);
        model.addAttribute("animalType", animalType);
        model.addAttribute("user", new User());
        return "searches/spaceOptions";
    }


}
