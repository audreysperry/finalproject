package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.models.*;
import com.audreysperry.finalproject.models.Thread;
import com.audreysperry.finalproject.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Date;
import java.util.List;

@Controller
public class MessagingController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ThreadRepository threadRepo;

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String homePage() {

        return "home";
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
       thread.setHost(host);
       thread.setGuestName(principal.getName());
       User guest = userRepo.findByUsername(principal.getName());
       thread.setGuest(guest);
       threadRepo.save(thread);
       message.setThread(thread);
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

        if (roleName.equals("ROLE_GENERAL")) {
            model.addAttribute("threads", threadRepo.findAllByGuest(user));

        } else {
            model.addAttribute("threads", threadRepo.findAllByHost(user));

        }

        return "messages/messageBoard";
    }

    @RequestMapping(value="thread/{thread_id}", method = RequestMethod.GET)
    public String displayMessages(Principal principal,
                                  Model model,
                                  @PathVariable("thread_id") long thread_id) {
        Thread currentThread = threadRepo.findOne(thread_id);
        User loggedInUser = userRepo.findByUsername(principal.getName());
        List<Message> currentMessages = currentThread.getMessages();
        for (Message message: currentMessages
             ) { if(message.getReceiver() == loggedInUser)
            message.setMessageRead(true);
             messageRepo.save(message);

        }
        model.addAttribute("thread", currentThread);
        model.addAttribute("messages", currentMessages);
        model.addAttribute("message", new Message());
        model.addAttribute("principal", principal);
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
