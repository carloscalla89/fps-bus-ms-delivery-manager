package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class FilterOrderDTO {
    private List<String> listOrderIds;
}
