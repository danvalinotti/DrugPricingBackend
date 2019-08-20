package com.galaxe.drugpriceapi.web.nap.postgresMigration.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.*;
@Component
@Getter
@Setter
@AllArgsConstructor
public class EmailConfig {
//    @Value("${spring.mail.host}")
    private  String host;

//    @Value("${spring.mail.port}")
    private  int port;

//    @Value("${spring.mail.username}")
    private  String username;
//    @Value("${spring.mail.password}")
    private  String password;

    public EmailConfig(){
        this.host = "smtp.mailtrap.io";
        this.port=465;
        this.username="ebe75f139d4c16";
        this.password="2dd9e2da6c65ae";

    }





}
