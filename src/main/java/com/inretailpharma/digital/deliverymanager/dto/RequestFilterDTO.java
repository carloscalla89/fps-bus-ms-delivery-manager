package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

@Data
public class RequestFilterDTO {

  private FiltersRqDTO filter;
  private Integer page;
  private Integer records;

}
