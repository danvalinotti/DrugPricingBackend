package com.galaxe.drugpriceapi.src.TableModels.listeners;


import com.galaxe.drugpriceapi.src.ResponseRequestObjects.GeneratePasswordResponse;
import com.galaxe.drugpriceapi.src.TableModels.Profile;
import com.galaxe.drugpriceapi.src.Controllers.EmailController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.PrePersist;
import java.util.ArrayList;
import java.util.List;

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
        GeneratePasswordResponse pass= new GeneratePasswordResponse();
        List<GeneratePasswordResponse> passes = new ArrayList<>();
        try {
            WebClient webClient = WebClient.create("https://passwordwolf.com/api/?special=off");
            passes = webClient.get().exchange().flatMapMany(clientResponse -> clientResponse.bodyToFlux(GeneratePasswordResponse.class)).collectList().block();

        }catch (Exception ex){
            ex.printStackTrace();
            GeneratePasswordResponse generatePasswordResponse = new GeneratePasswordResponse();
            generatePasswordResponse.setPassword("Galaxy123");
            passes.add(generatePasswordResponse);
        }

        profile.setPassword(passes.get(0).getPassword());

        this.emailController.sendEmailViaProfile(profile);
        profile.setPassword(BCrypt.hashpw(profile.getPassword(),BCrypt.gensalt()));
    }
}
