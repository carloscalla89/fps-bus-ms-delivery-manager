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

    @Column(name = "ean_code")
    private String eanCode;

    @Column(name = "name")
    private String productName;

    @Column(name = "short_description")
    private String shortDescription;

    private String brand;


    private Integer quantity;


    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "unit_price_w_discount")
    private BigDecimal unitPriceWDiscount;

    @Column(name = "total_price")
    private BigDecimal totalPrice;



    @Enumerated(EnumType.STRING)
    private Constant.Logical fractionated;

    @Column(name = "fractional_discount")
    private BigDecimal fractionalDiscount;

    @Column(name = "fractionated_price")
    private BigDecimal fractionatedPrice;

    @Column(name="presentation_id")
    private Integer presentationId;

    @Column(name="presentation_description")
    private String presentationDescription;

    @Column(name="quantity_units")
    private Integer quantityUnits;

    @Column(name="quantity_unit_minimium")
    private Integer quantityUnitMinimum;

    @Column(name="quantity_presentation")
    private Integer quantityPresentation;

    @Column(name="family_type")
    private String familyType;

    @Column(name="value_UMV")
    private Integer valueUMV;

    private BigDecimal priceList;
    private BigDecimal totalPriceList;
    private BigDecimal priceAllPaymentMethod;
    private BigDecimal totalPriceAllPaymentMethod;
    private BigDecimal priceWithpaymentMethod;
    private BigDecimal totalPriceWithpaymentMethod;
    private boolean crossOutPL;
    private String paymentMethodCardType;
    private BigDecimal promotionalDiscount;

}
