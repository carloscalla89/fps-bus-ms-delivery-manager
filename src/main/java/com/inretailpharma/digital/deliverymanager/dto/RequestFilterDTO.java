package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestFilterDTO {

  private FiltersRqDTO filter;
  private Integer page;
  private Integer records;
  //TODO: OMS
  private List<String> orderStatusCodeAllowed;
  private CriteriasRqDTO orderCriteria;
}
