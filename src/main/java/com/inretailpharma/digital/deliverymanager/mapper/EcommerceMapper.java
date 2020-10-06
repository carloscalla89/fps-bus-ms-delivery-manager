package com.inretailpharma.digital.deliverymanager.mapper;

import com.inretailpharma.digital.deliverymanager.canonical.integration.ProductCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.*;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EcommerceMapper {

    public OrderDto orderFulfillmentToOrderDto(IOrderFulfillment orderFulfillment,
                                               List<ProductCanonical> productsEcommerce,
                                               List<IOrderItemFulfillment> itemsFulfillment) {
        OrderDto orderDto = new OrderDto();

        if (Objects.nonNull(orderFulfillment)) {

            orderDto.setId(String.valueOf(orderFulfillment.getEcommerceId()));
            orderDto.setSource(orderFulfillment.getSource());
            orderDto.setDateCreated(Date.from(orderFulfillment.getCreatedOrder().atZone(ZoneId.systemDefault()).toInstant()));
            orderDto.setDiscountApplied(NumberUtils.DOUBLE_ZERO);

            AddressDto address = new AddressDto();
            address.setReceiverName(orderFulfillment.getAddressName());
            address.setDistrict(orderFulfillment.getDistrict());
            address.setLatitude(orderFulfillment.getLatitude().doubleValue());
            address.setLongitude(orderFulfillment.getLongitude().doubleValue());
            address.setNotes(orderFulfillment.getNotes());
            address.setApartment(orderFulfillment.getApartment());
            address.setStreet(orderFulfillment.getStreet());
            address.setCity(orderFulfillment.getCity());
            address.setCountry(orderFulfillment.getCountry());
            address.setDistrict(orderFulfillment.getDistrict());
            address.setNumber(orderFulfillment.getNumber());
            address.setReceiverName(orderFulfillment.getAddressReceiver());
            orderDto.setDeliveryAddress(address);

            UserDto userDto = new UserDto();

            userDto.setDni(orderFulfillment.getDocumentNumber());
            userDto.setName(orderFulfillment.getFirstName());
            userDto.setLastName(orderFulfillment.getLastName());
            userDto.setEmail(orderFulfillment.getEmail());
            userDto.setPhone(orderFulfillment.getPhone());
            userDto.setIsInkaClub(Constant.Logical.N.name());
            userDto.setIsAnonymous(Constant.Logical.Y.name());

            orderDto.setUser(userDto);

            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setDeliveryCost(orderFulfillment.getDeliveryCost().doubleValue());
            paymentDto.setDiscountApplied(NumberUtils.DOUBLE_ZERO);
            paymentDto.setGrossPrice(orderFulfillment.getTotalCost().doubleValue());
            paymentDto.setProductsTotalCost(orderFulfillment.getTotalCost().doubleValue());
            paymentDto.setProductsTotalCostNoDiscount(orderFulfillment.getTotalCost().subtract(orderFulfillment.getDeliveryCost()).doubleValue());

            orderDto.setPaymentAmountDto(paymentDto);
            orderDto.setDiscountApplied(NumberUtils.DOUBLE_ZERO);

            PaymentMethodDto paymentMethodDto = new PaymentMethodDto();
            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                paymentMethodDto.setId(Constant.DEFAULT_SC_PAYMENT_METHOD_ID);
                paymentMethodDto.setName(Constant.DEFAULT_SC_PAYMENT_METHOD_VALUE);
            }
            orderDto.setPaymentMethod(paymentMethodDto);

            ReceiptDto receipt = new ReceiptDto();

            ReceiptTypeDto receiptTypeDto = new ReceiptTypeDto();
            receiptTypeDto.setName(orderFulfillment.getReceiptType());

            receipt.setReceiptType(receiptTypeDto);
            receipt.setCompanyAddress(orderFulfillment.getCompanyAddressReceipt());
            receipt.setRuc(orderFulfillment.getRuc());
            receipt.setCompanyName(orderFulfillment.getCompanyNameReceipt());

            orderDto.setReceipt(receipt);
            orderDto.setCompanyCode(orderFulfillment.getCompanyCode());

            Map<String, IOrderItemFulfillment> productMap = itemsFulfillment.stream()
                    .collect(Collectors.toMap(IOrderItemFulfillment::getProductCode, p -> p));

            List<ItemDto> orderItems = new ArrayList<>();
            for (ProductCanonical product : productsEcommerce) {
                ItemDto itemDto = new ItemDto();
                itemDto.setBrand(productMap.get(product.getId()).getBrandProduct());
                itemDto.setFractionated(productMap.get(product.getId()).getFractionated());
                itemDto.setName(productMap.get(product.getId()).getNameProduct());
                itemDto.setQuantity(productMap.get(product.getId()).getQuantity());
                itemDto.setShortDescription(productMap.get(product.getId()).getShortDescriptionProduct());
                itemDto.setProductId(productMap.get(product.getId()).getProductCode());
                itemDto.setProductSapCode(product.getSapCode());
                itemDto.setEanCode(product.getEanCode());
                itemDto.setTotalPrice(productMap.get(product.getId()).getTotalPrice());
                itemDto.setUnitPrice(productMap.get(product.getId()).getUnitPrice());
                itemDto.setPresentationType(product.getPresentationId());
                itemDto.setPresentation(product.getPresentation());
                itemDto.setQuantityUnits(product.getQuantityUnits());
                itemDto.setFamilyType(product.getFamilyType());
                itemDto.setFractionatedPrice(product.getFractionatedPrice());
                orderItems.add(itemDto);
            }

            orderDto.setItems(orderItems);
            orderDto.setAmount(NumberUtils.DOUBLE_ZERO);
            orderDto.setCreditCardProviderId(Constant.DEFAULT_SC_CARD_PROVIDER_ID);
            orderDto.setDeliveryType(Constant.DEFAULT_DS);
            orderDto.setMarketplaceName(orderFulfillment.getSourceCompanyName());
        }

        return orderDto;
    }

    public OrderDto orderFulfillmentToOrderDto(IOrderFulfillment orderFulfillment,
                                               List<IOrderItemFulfillment> itemsFulfillment) {
        OrderDto orderDto = new OrderDto();

        if (Objects.nonNull(orderFulfillment)) {

            orderDto.setDeliveryServiceName(orderFulfillment.getServiceTypeCode());
            orderDto.setId(String.valueOf(orderFulfillment.getEcommerceId()));
            orderDto.setSource(orderFulfillment.getSource());
            orderDto.setDateCreated(Date.from(orderFulfillment.getCreatedOrder().atZone(ZoneId.systemDefault()).toInstant()));
            orderDto.setDiscountApplied(
                    Optional.ofNullable(orderFulfillment.getDiscountApplied())
                            .map(BigDecimal::doubleValue)
                            .orElse(NumberUtils.DOUBLE_ZERO)
            );

            AddressDto address = new AddressDto();
            address.setReceiverName(orderFulfillment.getAddressName());
            address.setDistrict(orderFulfillment.getDistrict());
            address.setLatitude(orderFulfillment.getLatitude().doubleValue());
            address.setLongitude(orderFulfillment.getLongitude().doubleValue());
            address.setNotes(orderFulfillment.getNotes());
            address.setApartment(orderFulfillment.getApartment());
            address.setStreet(orderFulfillment.getStreet());
            address.setCity(orderFulfillment.getCity());
            address.setCountry(orderFulfillment.getCountry());
            address.setDistrict(orderFulfillment.getDistrict());
            address.setNumber(orderFulfillment.getNumber());
            address.setReceiverName(orderFulfillment.getAddressReceiver());
            orderDto.setDeliveryAddress(address);

            UserDto userDto = new UserDto();

            userDto.setDni(orderFulfillment.getDocumentNumber());
            userDto.setName(orderFulfillment.getFirstName());
            userDto.setLastName(orderFulfillment.getLastName());
            userDto.setEmail(orderFulfillment.getEmail());
            userDto.setPhone(orderFulfillment.getPhone());
            userDto.setIsInkaClub(Constant.Logical.N.name());

            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                userDto.setIsAnonymous(Constant.Logical.Y.name());
            } else {
                userDto.setIsAnonymous(Optional.ofNullable(orderFulfillment.getAnonimous())
                        .orElse("0").equals("0") ?"N":"Y");
            }

            orderDto.setUser(userDto);

            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setDeliveryCost(orderFulfillment.getDeliveryCost().doubleValue());
            paymentDto.setDiscountApplied(Optional.ofNullable(orderFulfillment.getDiscountApplied())
                    .map(BigDecimal::doubleValue)
                    .orElse(NumberUtils.DOUBLE_ZERO)
            );
            paymentDto.setGrossPrice(orderFulfillment.getTotalCost().doubleValue());
            paymentDto.setProductsTotalCost(orderFulfillment.getTotalCost().doubleValue());
            paymentDto.setProductsTotalCostNoDiscount(orderFulfillment.getTotalCost().subtract(orderFulfillment.getDeliveryCost()).doubleValue());

            orderDto.setPaymentAmountDto(paymentDto);

            PaymentMethodDto paymentMethodDto = new PaymentMethodDto();
            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                paymentMethodDto.setId(Constant.DEFAULT_SC_PAYMENT_METHOD_ID);
                paymentMethodDto.setName(Constant.DEFAULT_SC_PAYMENT_METHOD_VALUE);
            } else {
                paymentMethodDto.setId(
                        PaymentMethod.PaymentType.getPaymentTypeByNameType(orderFulfillment.getPaymentType()).getId()
                );
                paymentMethodDto.setName(
                        PaymentMethod.PaymentType.getPaymentTypeByNameType(orderFulfillment.getPaymentType()).getDescription()
                );
            }

            orderDto.setPaymentMethod(paymentMethodDto);

            ReceiptDto receipt = new ReceiptDto();

            ReceiptTypeDto receiptTypeDto = new ReceiptTypeDto();
            receiptTypeDto.setName(orderFulfillment.getReceiptType());

            receipt.setReceiptType(receiptTypeDto);
            receipt.setCompanyAddress(orderFulfillment.getCompanyAddressReceipt());
            receipt.setRuc(orderFulfillment.getRuc());
            receipt.setCompanyName(orderFulfillment.getCompanyNameReceipt());

            orderDto.setReceipt(receipt);
            orderDto.setCompanyCode(orderFulfillment.getCompanyCode());


            List<ItemDto> orderItems = new ArrayList<>();
            for (IOrderItemFulfillment product : itemsFulfillment) {
                ItemDto itemDto = new ItemDto();
                itemDto.setBrand(product.getBrandProduct());
                itemDto.setFractionated(product.getFractionated());
                itemDto.setName(product.getNameProduct());
                itemDto.setQuantity(product.getQuantity());
                itemDto.setShortDescription(product.getShortDescriptionProduct());
                itemDto.setProductId(product.getProductCode());
                itemDto.setProductSapCode(product.getProductSapCode());
                itemDto.setEanCode(product.getEanCode());
                itemDto.setTotalPrice(product.getTotalPrice());
                itemDto.setUnitPrice(product.getUnitPrice());
                itemDto.setPresentationType(product.getPresentationId());
                itemDto.setPresentation(product.getPresentationDescription());
                itemDto.setQuantityUnits(product.getQuantityUnits());
                itemDto.setFamilyType(product.getFamilyType());
                itemDto.setFractionatedPrice(
                        Optional.ofNullable(product.getFractionatedPrice())
                                .map(BigDecimal::doubleValue)
                                .orElse(NumberUtils.DOUBLE_ZERO)
                );
                orderItems.add(itemDto);
            }

            orderDto.setItems(orderItems);
            orderDto.setAmount(Optional.ofNullable(orderFulfillment.getChangeAmount())
                    .map(BigDecimal::doubleValue)
                    .orElse(NumberUtils.DOUBLE_ZERO));
            orderDto.setCreditCardProviderId(Constant.DEFAULT_SC_CARD_PROVIDER_ID);

            if (Constant.Source.SC.name().equals(orderFulfillment.getSource())) {
                orderDto.setDeliveryType(Constant.DEFAULT_DS);
            } else {
                orderDto.setDeliveryType(orderFulfillment.getServiceTypeCode());
            }

            orderDto.setMarketplaceName(orderFulfillment.getSourceCompanyName());
        }

        return orderDto;
    }

}
