package com.galaxe.drugpriceapi.repositories;

import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoEntityRepository extends MongoRepository<MongoEntity, String> {

    Optional<MongoEntity> findById(String id);

    Optional<MongoEntity> findByNdcAndDosageStrengthAndQuantityAndZipcode(
            String ndc, String dosageStrength, String quantity, String zipcode);

    void deleteById(String id);
}
