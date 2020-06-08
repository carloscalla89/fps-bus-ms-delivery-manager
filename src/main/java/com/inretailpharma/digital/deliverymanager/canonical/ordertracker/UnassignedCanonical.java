package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnassignedCanonical implements Serializable  {

	private String groupName;
    private String motorizedId;
    private List<Long> orders;
}
