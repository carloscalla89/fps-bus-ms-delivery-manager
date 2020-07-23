package com.inretailpharma.digital.deliverymanager.mapper;

import com.inretailpharma.digital.deliverymanager.canonical.integration.ProductCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

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
            receipt.setCompanyName(orderFulfillment.getCompanyCode());

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
        }

        return orderDto;
    }
}
