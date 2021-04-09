package com.joytech.teamup.bl;

import com.joytech.teamup.controller.UserController;
import com.joytech.teamup.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ActivationListener implements ApplicationListener<OnActivationSuccessfulEvent> {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnActivationSuccessfulEvent event) {
        this.confirmActivation(event);
    }

    private void confirmActivation(OnActivationSuccessfulEvent event) {
        User user = event.getUser();

        String recipientAddress = user.getEmail();
        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(recipientAddress);
        msg.setSubject("Teamup activation confirmation");
        msg.setText(String.format("Hi %s %s,\nYour account using email %s was successfully activated.", user.getFirstName(), user.getLastName(), user.getEmail()));
        mailSender.send(msg);
    }
}
