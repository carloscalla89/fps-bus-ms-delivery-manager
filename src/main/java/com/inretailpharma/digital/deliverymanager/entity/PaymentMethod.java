package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;
import org.apache.commons.lang3.EnumUtils;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Embeddable
@Table(name="payment_method")
public class PaymentMethod {

    @Enumerated(EnumType.STRING)
    @Column(table = "payment_method", name="payment_type")
    private PaymentType paymentType;

    @Column(table = "payment_method", name="card_provider_id")
    private Integer cardProviderId;

    @Column(table = "payment_method", name="card_provider_code")
    private String cardProviderCode;

    @Column(table = "payment_method", name="card_provider")
    private String cardProvider;

    @Column(table = "payment_method", name="bin")
    private String bin;

    @Column(table = "payment_method", name="card_name")
    private String cardName;

    @Column(table = "payment_method", name="card_number")
    private String cardNumber;

    @Column(table = "payment_method", name="paid_amount")
    private BigDecimal paidAmount;

    @Column(table = "payment_method", name="change_amount")
    private BigDecimal changeAmount;

    @Column(table = "payment_method ", name="payment_note")
    private String paymentNote;

    @Column(table = "payment_method", name="coupon")
    private String coupon;

    @Column(table = "payment_method", name="payment_transaction_id")
    private String paymentTransactionId;

    public enum PaymentType {

        CASH(1,"CASH", "Efectivo"), CASH_DOLAR(1,"CASH_DOLAR", "Efectivo en dólares"),
        CARD(2,"POS", "Pago con P.O.S"), ONLINE_PAYMENT(3,"ONLINE", "Pago en línea"),
        MARKETPLACE(5, "MARKETPLACE", "Marketplace");

        private final Integer id;
        private final String code;
        private final String description;

        PaymentType(Integer id, String code, String description) {
            this.id = id;
            this.code = code;
            this.description = description;
        }

        public Integer getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static PaymentType getPaymentTypeByNameType(String type) {
            return EnumUtils.getEnumList(PaymentType.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(type))
                    .findFirst()
                    .orElse(CASH);
        }

    }
}
