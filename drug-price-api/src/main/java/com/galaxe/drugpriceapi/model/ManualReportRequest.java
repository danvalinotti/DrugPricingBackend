package com.galaxe.drugpriceapi.model;

import com.galaxe.drugpriceapi.web.nap.ui.MongoEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ManualReportRequest {
    List<MongoEntity> drugs;
    List<String> drugDetails;
    List<String> providers;
}

