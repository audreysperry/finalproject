package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.googleApi.GeoCodingInterface;
import com.audreysperry.finalproject.googleApi.GeoCodingResponse;
import com.audreysperry.finalproject.models.*;
import com.audreysperry.finalproject.repositories.*;
import com.audreysperry.finalproject.storage.StorageService;
import feign.Feign;
import feign.gson.GsonDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.security.Principal;
import java.util.List;

@Controller
public class HostController {

    @Autowired
    private StorageService storageService;


    @Autowired
    private UserRepository userRepo;

    @Autowired
    private HostLocationRepository locationRepo;

    @Autowired
    private SpaceRepository spaceRepo;

    @Autowired
    private RoleRepository roleRepo;
    

    @Autowired
    private BookingRequestRepository bookingRepo;

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

        String apiKey = System.getenv("GOOGLE_MAPS_GEOCODING_API_KEY");
        GeoCodingInterface geoCodingInterface = Feign.builder()
                .decoder(new GsonDecoder())
                .target(GeoCodingInterface.class, "https://maps.googleapis.com");

        GeoCodingResponse response = geoCodingInterface.geoCodingResponse(locationString, apiKey);

        double lat = response.getResults().get(0).getGeometry().getLocation().getLat();
        double lng = response.getResults().get(0).getGeometry().getLocation().getLng();
        hostLocation.setLatitude(lat);
        hostLocation.setLongitude(lng);
        locationRepo.save(hostLocation);
        model.addAttribute("location_id", hostLocation.getId());
        model.addAttribute("space", new Space());
        return "home";

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
                           @RequestParam("location_id") long location_id,
                           @RequestParam("file")MultipartFile file) {
        HostLocation currentHostLocation = locationRepo.findById(location_id);
        space.setHostLocation(currentHostLocation);
        storageService.store(file);
        String fileName = file.getOriginalFilename();
        space.setImagePath(fileName);
        space.setActive(true);
        spaceRepo.save(space);
        return "redirect:/editHost";
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
        HostLocation hostLocation = locationRepo.findOne(location_id);
        space.setHostLocation(hostLocation);
        space.setActive(true);
        spaceRepo.save(space);

        return "redirect:/editHost";
    }

    @RequestMapping(value="/space/{spaceid}/deactivate", method = RequestMethod.POST)
    public String deactivateSpaceListing(Principal principal,
                                         @PathVariable("spaceid") long spaceid,
                                         Model model) {
        Space space = spaceRepo.findOne(spaceid);
        User currentUser = userRepo.findByUsername(principal.getName());



        if (space.getHostLocation().getUser() == currentUser) {
            space.setActive(false);
            spaceRepo.save(space);
            List<BookingRequest> openRequests = bookingRepo.findAllBySpace(space);
            for (BookingRequest openReq : openRequests
                    ) { openReq.setHostResponse(false);
                bookingRepo.save(openReq);

            }
        }

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
}
