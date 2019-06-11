package com.galaxe.drugpriceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class DrugPriceApiApplication {


    public static void main(String[] args) {
        SpringApplication.run(DrugPriceApiApplication.class, args);
    }

}
