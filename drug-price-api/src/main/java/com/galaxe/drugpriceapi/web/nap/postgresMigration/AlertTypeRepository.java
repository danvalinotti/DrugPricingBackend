package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.AlertType;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.DrugRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertTypeRepository extends JpaRepository<AlertType,Integer> {

}
