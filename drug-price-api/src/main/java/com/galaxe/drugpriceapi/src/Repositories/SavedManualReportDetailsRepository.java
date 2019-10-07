package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.SavedManualReportDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedManualReportDetailsRepository extends JpaRepository<SavedManualReportDetails,Integer> {

    List<SavedManualReportDetails> findByUserId(int i);
}
