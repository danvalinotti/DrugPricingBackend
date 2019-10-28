package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.LeadingDrugs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadingDrugsRepository extends JpaRepository<LeadingDrugs, Integer> {

//    @Query(value= "SELECT * FROM leading_drugs where report_id = ?1",nativeQuery = true)
    List<LeadingDrugs> findByReportId(Integer reportId);
}
