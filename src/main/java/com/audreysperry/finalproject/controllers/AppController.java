package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.googleApi.GeoCodingInterface;
import com.audreysperry.finalproject.googleApi.GeoCodingResponse;
import com.audreysperry.finalproject.models.*;
import com.audreysperry.finalproject.models.Thread;
import com.audreysperry.finalproject.repositories.*;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ThreadRepository threadRepo;

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
        userRepo.save(currentUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(currentUser, currentUser.getPassword(), currentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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

    @RequestMapping(value= "/addSpace", method = RequestMethod.GET)
    public String addSpace(Model model,
                           Principal principal) {
        User currentUser = userRepo.findByUsername(principal.getName());
        HostLocation hostLocation = locationRepo.findByUser(currentUser);

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
        return "redirect:/editHost";
    }

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

    @RequestMapping(value="/editHost", method = RequestMethod.GET)
    public String viewHostInfo(Principal principal,
                               Model model) {
        User currentUser = userRepo.findByUsername(principal.getName());
        HostLocation location = locationRepo.findByUser(currentUser);
        model.addAttribute("location", location);
        model.addAttribute("user", currentUser);

        return "viewHostInfo";
    }

    @RequestMapping(value="/location/{id}/edit", method = RequestMethod.GET)
    public String editLocationInfo(Principal principal,
                                   Model model,
                                   @PathVariable("id") long id) {
        HostLocation currentLocation = locationRepo.findOne(id);
        User currentUser = userRepo.findByUsername(principal.getName());
        if (currentUser == currentLocation.getUser()) {
            model.addAttribute("location", currentLocation);
            return "locationEditForm";
        } else {
            return "home";
        }

    }

    @RequestMapping(value="/location/{id}/edit", method = RequestMethod.POST)
    public String  updateLocationInfo(Principal principal,
                                    @ModelAttribute HostLocation hostLocation) {
        User currentUser = userRepo.findByUsername(principal.getName());
        hostLocation.setUser(currentUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(currentUser, currentUser.getPassword(), currentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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

        return "redirect:/editHost";

    }

    @RequestMapping(value="/space/{id}/edit", method = RequestMethod.GET)
    public String editSpaceInfo(Principal principal,
                                Model model,
                                @PathVariable("id") long id) {
        User currentUser = userRepo.findByUsername(principal.getName());
        Space space = spaceRepo.findOne(id);
        HostLocation hostLocation = space.getHostLocation();

        if (currentUser == hostLocation.getUser()) {
            model.addAttribute("space", space);
            model.addAttribute("location", space.getHostLocation().getId());

            return "spaceEditForm";
        } else {
            return "home";
        }
    }

    @RequestMapping(value="/space/{id}/edit", method = RequestMethod.POST)
    public String updateSpaceInfo(Principal principal,
                                  @ModelAttribute Space space,
                                  @RequestParam("location_id") long location_id) {
        System.out.println(location_id);
        HostLocation hostLocation = locationRepo.findOne(location_id);
        space.setHostLocation(hostLocation);
        spaceRepo.save(space);

        return "redirect:/editHost";
    }

    @RequestMapping(value="/addLocationImg/{locationid}", method = RequestMethod.POST)
    public String addLocationImg(Principal principal,
                                 @PathVariable("locationid") long locationid,
                                 Model model,
                                 @RequestParam("locationImage") String image) {
        System.out.println(image);
        return "editHost";
    }

    @RequestMapping(value="/messageHost/{user_id}", method= RequestMethod.GET)
    public String messageHostForm(@PathVariable("user_id") long user_id,
                                  Model model,
                                  Principal principal) {
        User host = userRepo.findOne(user_id);
        User guest = userRepo.findByUsername(principal.getName());

        model.addAttribute("host", host);
        model.addAttribute("guest", guest);
        model.addAttribute("message", new Message());

        return "/messages/messageHostForm";
    }

    @RequestMapping(value="/addMessage/{user_id}", method = RequestMethod.POST)
    public String addMessage(@ModelAttribute Message message,
                             Principal principal,
                             @PathVariable("user_id") long user_id) {

       User host = userRepo.findOne(user_id);
       Thread thread = new Thread();
       thread.setHostName(host.getUsername());
       thread.setGuestName(principal.getName());
       threadRepo.save(thread);
       message.setThread(thread);
       User guest = userRepo.findByUsername(principal.getName());
       message.setAuthorUsername(guest.getUsername());
       message.setRecipient(host.getUsername());
       message.setDate(new Date());
       message.setReceiver(host);
       message.setSender(guest);
       messageRepo.save(message);


        return "home";

    }

    @RequestMapping(value="/messages", method = RequestMethod.GET)
    public String showMessageBoard(Principal principal,
                                   Model model) {
        User user = userRepo.findByUsername(principal.getName());
        Role role = user.getRole();
        String roleName = role.getName();
        System.out.println(roleName);

        if (roleName.equals("ROLE_GENERAL")) {
            model.addAttribute("threads", threadRepo.findAllByGuest(user));

        } else {
            model.addAttribute("threads", threadRepo.findAllByHost(user));

        }
        System.out.println(model);
        return "messages/messageBoard";
    }

    @RequestMapping(value="thread/{thread_id}", method = RequestMethod.GET)
    public String displayMessages(Principal principal,
                                  Model model,
                                  @PathVariable("thread_id") long thread_id) {
        Thread currentThread = threadRepo.findOne(thread_id);
        List<Message> currentMessages = currentThread.getMessages();

        model.addAttribute("thread", currentThread);
        model.addAttribute("messages", currentMessages);
        model.addAttribute("message", new Message());
        return "messages/messageThreadDetails";
    }

    @RequestMapping(value="replyMessage/{thread_id}", method = RequestMethod.POST)
    public String replyMessage(Principal principal,
                               Model model,
                               @PathVariable("thread_id") long thread_id,
                               @ModelAttribute Message message) {
        Thread currentThread = threadRepo.findOne(thread_id);
        User currentUser = userRepo.findByUsername(principal.getName());
        message.setThread(currentThread);
        message.setDate(new Date());
        message.setSender(currentUser);
        User one = currentThread.getGuest();
        User two = currentThread.getHost();
        if (currentUser == one) {
            message.setReceiver(two);
            message.setRecipient(two.getUsername());
        } else {
            message.setReceiver(one);
            message.setRecipient(one.getUsername());
        }
        message.setAuthorUsername(currentUser.getUsername());

        messageRepo.save(message);
        List<Message> currentMessages = currentThread.getMessages();
        model.addAttribute("thread", currentThread);
        model.addAttribute("messages", currentMessages);
        model.addAttribute("message", new Message());


        return "messages/messageThreadDetails";


    }
}
