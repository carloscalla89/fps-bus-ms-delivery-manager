package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ShelfCanonical;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PickUpDetailsCanonical implements Serializable {
	
	private List<ShelfCanonical> shelfList;
	private String payBackEnvelope;

}
