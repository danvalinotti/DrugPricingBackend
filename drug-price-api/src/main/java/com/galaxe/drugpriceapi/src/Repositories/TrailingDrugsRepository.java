package com.galaxe.drugpriceapi.src.Repositories;

import com.galaxe.drugpriceapi.src.TableModels.TrailingDrugs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrailingDrugsRepository extends JpaRepository<TrailingDrugs, Integer> {
    List<TrailingDrugs> findByReportId(Integer reportId);
}
