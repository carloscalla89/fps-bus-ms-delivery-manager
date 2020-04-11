package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectedGroupCanonical implements Serializable {

    private String groupName;
    private String motorizedId;
    private Integer projectedEtaReturn;
    private Integer drugstoreId;
    private List<GroupCanonical> group;
    private String statusDrugstore;

}
