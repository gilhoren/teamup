package com.joytech.teamup.controller;

import com.joytech.teamup.bl.OnActivationSuccessfulEvent;
import com.joytech.teamup.bl.OnRegistrationCompletedEvent;
import com.joytech.teamup.dao.UserRepository;
import com.joytech.teamup.dao.VerificationTokenRepository;
import com.joytech.teamup.dto.EmailAndPassword;
import com.joytech.teamup.dto.User;
import com.joytech.teamup.dto.VerificationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
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
    public User addNewUser(@RequestBody User user, HttpServletRequest request) {
        User registeredUser;
        try {
            System.out.println(user.getFirstName());
            // validate user
//            if (!validateUser(user)) {
//                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.format("User with email %s already exists", user.getEmail()));
//            }
            registeredUser = userRepository.save(user);
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompletedEvent(registeredUser, request.getLocale(), appUrl));
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
            System.out.println(ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ((ResponseStatusException)ex).getReason(), ex);
        }
        return registeredUser;
    }

    @CrossOrigin
    @GetMapping("v1/regitrationConfirm")
    public boolean confirmRegistration (@RequestParam("token") String token, @RequestParam("id") int userId, HttpServletRequest request) throws ResponseStatusException {
        getVerificationToken(token, userId, request);
        return true;
    }

    @CrossOrigin
    @PostMapping("v1/authenticate")
    @ResponseBody
    public User authenticate (@RequestBody EmailAndPassword emailAndPassword) throws Exception {
        return getUserByEmailAndValidate(emailAndPassword.getEmail(), emailAndPassword.getPassword());
    }

    private boolean validateUser(User user) {
        List<User> users = userRepository.findUserByEmail(user.getEmail());
        if (users != null && users.size() > 0) {
            return false;
        }
        return true;
    }

    private User getUserByEmailAndValidate(String email, String password) throws Exception {
        List<User> users = userRepository.findUserByEmail(email);
        if (users == null || users.size() == 0 || !users.stream().anyMatch(u -> u.getPassword().equals(password))) {
            String errMsg = String.format("user %s was not found or password does not match", email);
            System.out.println(errMsg);
            // return an invalid user
            User u = new User();
            u.setId(-1);
            return u;
        }
        return users.get(0);
    }

    private boolean getVerificationToken(String token, int userId, HttpServletRequest request) throws ResponseStatusException {
        List<VerificationToken> tokens = verificationTokenRepository.find(token);
        if (tokens == null || tokens.size() == 0 || !tokens.stream().anyMatch(t -> t.getUser().getId().equals(userId))) {
            String errMsg = String.format("token %s was not found for the specified user %s", token, userId);
            System.out.println(errMsg);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
        }

        VerificationToken verificationToken = tokens.stream().filter(t -> t.getUser().getId().equals(userId)).findFirst().get();

        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            String errMsg = String.format("token %s is already expired for the specified user %s", token, userId);
            System.out.println(errMsg);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errMsg);
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        String appUrl = request.getContextPath();
        eventPublisher.publishEvent(new OnActivationSuccessfulEvent(user, request.getLocale(), appUrl));

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
