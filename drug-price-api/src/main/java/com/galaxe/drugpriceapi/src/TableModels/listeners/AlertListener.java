package com.galaxe.drugpriceapi.src.TableModels.listeners;


import com.galaxe.drugpriceapi.src.TableModels.Alert;
import com.galaxe.drugpriceapi.src.Controllers.EmailController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.persistence.PrePersist;

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
        this.emailController.sendEmailViaAlert(alert);
    }
//
}