package com.joytech.teamup.bl;

import com.joytech.teamup.controller.UserController;
import com.joytech.teamup.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompletedEvent> {

    @Autowired
    private UserController service;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompletedEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompletedEvent event) {
        User user = event.getUser();

        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String token = String.format("%06d", number);

        service.createVerificationToken(user, token);

        String recipientAddress = user.getEmail();
        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(recipientAddress);
        msg.setSubject("Teamup activation confirmation token");
        msg.setText(String.format("You're only a few clicks away.\nHere's your activation token: %s.\nIt will be expired in an hour\n\nThanks for using Teamup.", token));
        mailSender.send(msg);
    }
}
