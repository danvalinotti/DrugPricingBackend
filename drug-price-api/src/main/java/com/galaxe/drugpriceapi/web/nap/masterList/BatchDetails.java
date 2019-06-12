package com.galaxe.drugpriceapi.web.nap.masterList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class BatchDetails {
    int batchNumber;
    Date batchStart;
}
