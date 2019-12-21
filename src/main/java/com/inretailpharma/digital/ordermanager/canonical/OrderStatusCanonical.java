package com.inretailpharma.digital.ordermanager.canonical;

import com.inretailpharma.digital.ordermanager.util.Constant;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderStatusCanonical implements Serializable {

    private Constant.OrderStatus status;
    private String statusCode;
    private String statusDetail;

}
