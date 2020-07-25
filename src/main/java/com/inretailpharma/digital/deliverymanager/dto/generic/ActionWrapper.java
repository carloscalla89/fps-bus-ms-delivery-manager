package com.inretailpharma.digital.deliverymanager.dto.generic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionWrapper<T> {
    private String action;
    private T body;
}
