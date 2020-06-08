package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.manager.CenterCompanyCanonical;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectedGroupCanonical implements Serializable {

    private String groupName;
    private String motorizedId;
    private Integer projectedEtaReturn;
    private List<GroupCanonical> group;
    private CenterCompanyCanonical pickUpCenter;

}
