package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.GenericResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTrackerResponseCanonical extends GenericResponseDto implements Serializable {

    private Long ecommerceId;

}
