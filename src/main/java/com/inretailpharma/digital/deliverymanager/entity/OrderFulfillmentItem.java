package com.inretailpharma.digital.deliverymanager.entity;

import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Embeddable
@Table(name="order_fulfillment_item")
public class OrderFulfillmentItem {

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_sap_code")
    private String productSapCode;

    @Column(name = "name")
    private String productName;

    @Column(name = "short_description")
    private String shortDescription;
    private String brand;



    private Integer quantity;

    @Column(name = "old_quantity")
    private Integer oldQuantity;

    @Column(name = "modified_quantity")
    private Integer modifiedQuantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "unit_price_w_discount")
    private BigDecimal unitPriceWDiscount;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "modified_total_price")
    private BigDecimal modifiedTotalPrice;

    @Enumerated(EnumType.STRING)
    private Constant.Logical fractionated;

    @Column(name = "fractional_discount")
    private BigDecimal fractionalDiscount;

    @Column(name = "old_fractional_discount")
    private BigDecimal oldFractionalDiscount;

    private Integer partial;

    @Column(name="edition_code_type")
    private String editionCodeType;

}
