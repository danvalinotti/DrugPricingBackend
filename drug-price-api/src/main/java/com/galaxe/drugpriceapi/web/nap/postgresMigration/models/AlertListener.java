package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;


import com.galaxe.drugpriceapi.web.nap.postgresMigration.AlertTypeRepository;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.EmailController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.persistence.PrePersist;
import java.util.List;

@Component
public class AlertListener {

//    static AlertTypeRepository alertTypeRepository;
    static EmailController emailController;

    @Autowired
    public void init(EmailController emailController)
    {
        this.emailController = emailController;

    }

    @PrePersist
    public void prePersist(Alert alert) {
//        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        this.emailController.sendEmailViaAlert(alert);

    }
}
