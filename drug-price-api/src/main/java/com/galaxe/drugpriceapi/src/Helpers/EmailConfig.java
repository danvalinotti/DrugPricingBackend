package com.galaxe.drugpriceapi.src.Helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@AllArgsConstructor
public class EmailConfig {

    private  String host;

    private  int port;

    private  String username;

    private  String password;

    public EmailConfig(){
        this.host = "10.1.10.27";
        this.port=25;
//        this.username="ebe75f139d4c16";
//        this.password="2dd9e2da6c65ae";

    }





}
