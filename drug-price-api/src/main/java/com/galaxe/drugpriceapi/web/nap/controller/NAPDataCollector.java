package com.galaxe.drugpriceapi.web.nap.controller;

import com.galaxe.drugpriceapi.repositories.DrugNAPRepository;
import com.galaxe.drugpriceapi.web.nap.mapper.NAPMapper;
import com.galaxe.drugpriceapi.web.nap.model.DrugPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;


@RestController
public class NAPDataCollector {

    @Autowired
    NAPMapper napMapper;

    @Autowired
    DrugNAPRepository drugNAPRepository;

    @RequestMapping("/loadNAP")
    public void loadNAP() {
        WebClient webClient = WebClient.create("https://data.medicaid.gov/resource/tau9-gfwr.json");
        webClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMapMany(clientResponse -> clientResponse.bodyToFlux(DrugPrice.class))
                .subscribe(drugPrice -> {
                    drugNAPRepository.save(napMapper.convertToMongoModel(drugPrice));
                });
    }

}
