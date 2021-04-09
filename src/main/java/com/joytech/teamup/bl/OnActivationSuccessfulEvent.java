package com.joytech.teamup.bl;

import com.joytech.teamup.dto.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
@Setter
public class OnActivationSuccessfulEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private User user;

    public OnActivationSuccessfulEvent(User user, Locale locale, String appUrl) {
        super(user);
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}
