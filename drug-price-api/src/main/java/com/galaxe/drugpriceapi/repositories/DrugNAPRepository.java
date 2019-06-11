package com.galaxe.drugpriceapi.repositories;

import com.galaxe.drugpriceapi.model.DrugNAP;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DrugNAPRepository extends MongoRepository<DrugNAP, String> {

    Optional<DrugNAP> findByndc(String ndc);
}
