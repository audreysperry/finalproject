package com.audreysperry.finalproject.controllers;


import com.audreysperry.finalproject.models.Role;
import com.audreysperry.finalproject.models.User;
import com.audreysperry.finalproject.repositories.RoleRepository;
import com.audreysperry.finalproject.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private BCryptPasswordEncoder bCyrptPasswordEncoder;

    @RequestMapping(value="/login", method = RequestMethod.GET)
    public String loginForm(Model model,
                            HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("url_prior_login", referrer);

        model.addAttribute("user", new User());
        Object message = request.getSession().getAttribute("error");
        model.addAttribute("errors", message);
        request.getSession().removeAttribute("error");

        return "login";
    }

    @RequestMapping(value="/signup", method = RequestMethod.GET)
    public String signupForm(Model model) {
        model.addAttribute("user", new User());

        return "signup";
    }

    @RequestMapping(value="/signup", method = RequestMethod.POST)
    public String signup(@ModelAttribute User user) {
        Role userRole = roleRepo.findByName("ROLE_GENERAL");
        user.setRole(userRole);

        String encryptedPassword = bCyrptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        user.setActive(true);
        userRepo.save(user);
        return "redirect:/login";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        request.getSession().removeAttribute("url_prior_login");
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/";
    }
}
