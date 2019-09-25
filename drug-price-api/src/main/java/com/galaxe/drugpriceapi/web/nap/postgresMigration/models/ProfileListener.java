package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;


import com.galaxe.drugpriceapi.web.nap.postgresMigration.EmailController;
import com.galaxe.drugpriceapi.web.nap.ui.ZipcodeConverter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.persistence.PrePersist;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ProfileListener {

//    static AlertTypeRepository alertTypeRepository;
    static EmailController emailController;

    @Autowired
    public void init(EmailController emailController)
    {
        this.emailController = emailController;

    }


    @PrePersist
    public void prePersist(Profile profile) {
//        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        RandomPassword pass= new RandomPassword();
        List<RandomPassword> passes = new ArrayList<>();
        try {
            WebClient webClient = WebClient.create("https://passwordwolf.com/api/?special=off");
            passes = webClient.get().exchange().flatMapMany(clientResponse -> clientResponse.bodyToFlux(RandomPassword.class)).collectList().block();

        }catch (Exception ex){
            ex.printStackTrace();
            RandomPassword randomPassword = new RandomPassword();
            randomPassword.setPassword("Galaxy123");
            passes.add(randomPassword);
        }

        profile.setPassword(passes.get(0).getPassword());

        this.emailController.sendEmailViaProfile(profile);
        profile.setPassword(BCrypt.hashpw(profile.getPassword(),BCrypt.gensalt()));
    }
}
