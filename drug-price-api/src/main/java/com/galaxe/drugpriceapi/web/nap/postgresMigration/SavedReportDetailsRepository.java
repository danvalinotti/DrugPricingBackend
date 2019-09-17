package com.galaxe.drugpriceapi.web.nap.postgresMigration;

import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.Dashboard;
import com.galaxe.drugpriceapi.web.nap.postgresMigration.models.SavedReportDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedReportDetailsRepository extends JpaRepository<SavedReportDetails,Integer> {

    List<SavedReportDetails> findByUserId(int i);
}
