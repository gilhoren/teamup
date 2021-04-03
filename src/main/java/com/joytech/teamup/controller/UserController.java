package com.joytech.teamup.controller;

import com.joytech.teamup.bl.OnRegistrationCompletedEvent;
import com.joytech.teamup.dao.UserRepository;
import com.joytech.teamup.dao.VerificationTokenRepository;
import com.joytech.teamup.dto.User;
import com.joytech.teamup.dto.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path="/teamup/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    // TODO: 1. Error handling - return the error message


    @CrossOrigin
    @PostMapping(path="/v1/add")
    @ResponseBody
    public String addNewUser(@RequestBody User user, HttpServletRequest request) {
        User registeredUser = null;
        try {
            System.out.println(user.getFirstName());
            registeredUser = userRepository.save(user);
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompletedEvent(registeredUser, request.getLocale(), appUrl));
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
            System.out.println(ex.getMessage());
            return "Err";
        }
        // TODO: handle the logic for error handling
        return registeredUser.getId().toString();
    }

    @CrossOrigin
    @GetMapping("v1/regitrationConfirm")
    public String confirmRegistration (@RequestParam("token") String token, @RequestParam("id") int userId) throws Exception {
        getVerificationToken(token, userId);
        return "Saved";
    }

    private boolean getVerificationToken(String token, int userId) throws Exception {
        List<VerificationToken> tokens = verificationTokenRepository.find(token);
        if (tokens == null || tokens.size() == 0 || !tokens.stream().anyMatch(t -> t.getUser().getId().equals(userId))) {
            String errMsg = String.format("token %s was not found for the specified user %s", token, userId);
            System.out.println(errMsg);
            throw new Exception(errMsg);
        }

        VerificationToken verificationToken = tokens.stream().filter(t -> t.getUser().getId().equals(userId)).findFirst().get();

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String errMsg = String.format("token %s is already expired for the specified user %s", token, userId);
            System.out.println(errMsg);
            throw new Exception(errMsg);
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return true;
    }

    @GetMapping(path="/v1/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void createVerificationToken(User user, String token) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);
    }
}
