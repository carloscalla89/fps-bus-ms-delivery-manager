package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.canonical.manager.DetailProduct;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfo;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoAdditional;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoClient;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoPaymentMethodDto;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfoProduct;
import com.inretailpharma.digital.deliverymanager.dto.OrderInfoConsolidated;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderInfoClient;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderInfoPaymentMethod;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderInfoProduct;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderInfoProductDetail;
import com.inretailpharma.digital.deliverymanager.repository.OrderRepository;
import com.inretailpharma.digital.deliverymanager.service.OrderInfoService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.Constant.DeliveryType;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService {

  private OrderRepository orderRepository;

  @Override
  public Mono<OrderInfoConsolidated> findOrderInfoClientByEcommerceId(long ecommerceId) {

    OrderInfoConsolidated orderInfoConsolidated = new OrderInfoConsolidated();
    OrderInfoClient orderInfoClient = getOrderInfoClientByEcommerceId(ecommerceId,orderInfoConsolidated);
    OrderInfoPaymentMethodDto orderInfoPaymentMethod = getOrderInfoPaymentMethodByEcommercerId(ecommerceId);
    OrderInfoProduct orderInfoProduct = getOrderInfoProductByEcommerceId(ecommerceId);
    orderInfoConsolidated.setOrderInfoClient(orderInfoClient);
    orderInfoConsolidated.setPaymentMethodDto(orderInfoPaymentMethod);
    orderInfoConsolidated.setProductDetail(orderInfoProduct);
    return  Mono.justOrEmpty(orderInfoConsolidated);

  }

  private OrderInfo getOrderInfo(IOrderInfoClient orderInfoProjection) {
    OrderInfo orderInfo = new OrderInfo();
    orderInfo.setOrderId(orderInfoProjection.getOrderId());
    orderInfo.setCompanyCode(orderInfoProjection.getCompanyCode());
    orderInfo.setEcommerceId(orderInfoProjection.getEcommerceId());
    orderInfo.setLocalCode(orderInfoProjection.getLocalCode());
    orderInfo.setOrderType(orderInfoProjection.getOrderType());
    orderInfo.setScheduledTime(DateUtils.getLocalDateTimeWithFormatDDMMYY_AMPM(orderInfoProjection.getScheduledTime()));
    orderInfo.setStatusName(orderInfoProjection.getStatusName());
    orderInfo.setServiceTypeShortCode(DeliveryType.getByName(orderInfoProjection.getServiceTypeShortCode()).getDescription());
    orderInfo.setServiceChannel(orderInfoProjection.getServiceChannel());
    orderInfo.setServiceType(getServiceType(orderInfoProjection.getServiceType()));
    return orderInfo;
  }






  private String getServiceType(String serviceType) {
    List<String> services = Arrays.asList(serviceType.split("_"));
    if (CollectionUtils.isNotEmpty(services)) {
      if (services.stream().anyMatch(service -> service.equalsIgnoreCase("RET"))) {
        return "RET_LITE";
      } else {
        if (services.get(0).equalsIgnoreCase("INKATRACKER") && services.get(1)
            .equalsIgnoreCase("LITE")) {
          return "RAD_LITE";
        } else {
          return "RAD_DC";
        }
      }
    }

    return StringUtils.EMPTY;

  }

  private OrderInfoProduct getOrderInfoProductByEcommerceId(long ecommerceId) {
    IOrderInfoProduct orderInfoProduct = orderRepository.getOrderInfoProductByEcommerceId(ecommerceId);
    if (orderInfoProduct != null) {
      List<IOrderInfoProductDetail> orderInfoProductDetail = orderRepository.getOrderInfoProductDetailByOrderFulfillmentId(orderInfoProduct.getId());
      return getOrderInfoProduct(orderInfoProduct, orderInfoProductDetail);
    }
    return null;
  }

  private OrderInfoProduct getOrderInfoProduct(IOrderInfoProduct orderInfoProduct, List<IOrderInfoProductDetail> orderInfoProductDetail) {
    OrderInfoProduct orderInfo = new OrderInfoProduct();
    orderInfo.setDeliveryAmount(orderInfoProduct.getDeliveryAmount());
    orderInfo.setTotalDiscount(orderInfoProduct.getTotalDiscount());
    orderInfo.setTotalImport(orderInfoProduct.getTotalImport());
    orderInfo.setTotalImportWithOutDiscount(orderInfoProduct.getTotalImportWithOutDiscount());
    List<DetailProduct> detail = orderInfoProductDetail.stream().map(orderDetailEntity -> {
      DetailProduct detailProduct = new DetailProduct();
      detailProduct.setName(orderDetailEntity.getName());
      detailProduct.setQuantity(orderDetailEntity.getQuantity());
      //TODO: OMS
      detailProduct.setPresentationDescription(orderDetailEntity.getPresentationDescription());
      detailProduct.setSku(orderDetailEntity.getSku());
      detailProduct.setTotalPrice(orderDetailEntity.getTotalPrice());
      detailProduct.setUnitPrice(orderDetailEntity.getUnitPrice());
      return detailProduct;
    }).collect(Collectors.toList());

    orderInfo.setProducts(detail);

    return orderInfo;

  }

  private OrderInfoPaymentMethodDto getOrderInfoPaymentMethodByEcommercerId(long ecommerceId) {
    IOrderInfoPaymentMethod orderInfoProjection = orderRepository.getInfoPaymentMethod(ecommerceId);
    if(orderInfoProjection!=null){
      OrderInfoPaymentMethodDto oderInfoDto  = new OrderInfoPaymentMethodDto();
      oderInfoDto.setCardBrand(orderInfoProjection.getCardBrand());
      oderInfoDto.setLiquidationStatus(orderInfoProjection.getLiquidationStatus());
      oderInfoDto.setCardNumber(orderInfoProjection.getCardNumber());
      oderInfoDto.setChangeAmount(orderInfoProjection.getChangeAmount());
      oderInfoDto.setCodAuthorization(orderInfoProjection.getCodAuthorization());
      oderInfoDto.setFinancial(orderInfoProjection.getFinancial());
      oderInfoDto.setPaymentGateway(orderInfoProjection.getPaymentGateway());
      oderInfoDto.setServiceTypeCode(orderInfoProjection.getServiceTypeCode());
      oderInfoDto.setPaymentType(orderInfoProjection.getPaymentType());
      oderInfoDto.setPaymentDate(orderInfoProjection.getDateConfirmed());
      return oderInfoDto;
    }

    return null;

  }

  private OrderInfoClient getOrderInfoClientByEcommerceId(long ecommerceId, OrderInfoConsolidated orderInfoConsolidated) {
    IOrderInfoClient orderInfoProjection = orderRepository.getOrderInfoClientByEcommercerId(ecommerceId);
    if(orderInfoProjection!=null){
      OrderInfoClient orderInfoDto = new OrderInfoClient();
      orderInfoDto.setAddressClient(orderInfoProjection.getAddressClient());
      orderInfoDto.setClientName(orderInfoProjection.getClientName());
      orderInfoDto.setCoordinates(orderInfoProjection.getCoordinates());
      orderInfoDto.setDocumentNumber(orderInfoProjection.getDocumentNumber());
      orderInfoDto.setEmail(orderInfoProjection.getEmail());
      orderInfoDto.setReference(Optional.ofNullable(orderInfoProjection.getReference()).orElse("-"));
      orderInfoDto.setPhone(orderInfoProjection.getPhone());
      orderInfoDto.setCompanyCode(orderInfoProjection.getCompanyCode());
      orderInfoDto.setRuc(orderInfoProjection.getRuc());
      orderInfoDto.setCompanyName(orderInfoProjection.getCompanyName());
      orderInfoConsolidated.setOrderInfo(getOrderInfo(orderInfoProjection));
      orderInfoConsolidated.setOrderInfoAdditional(getOrderInfoAdditional(orderInfoProjection));
      return orderInfoDto;
    }
    return null;

  }

  private OrderInfoAdditional getOrderInfoAdditional(IOrderInfoClient orderInfoProjection) {

    OrderInfoAdditional orderInfo = new OrderInfoAdditional();
    orderInfo.setCancellationReason(Optional.ofNullable(orderInfoProjection.getCancelReason()).orElse("-"));
    orderInfo.setEcommerceId(orderInfoProjection.getEcommerceId());
    orderInfo.setLocalCode(Optional.ofNullable(orderInfoProjection.getLocalCode()).orElse("-"));
    orderInfo.setObservation(Optional.ofNullable(orderInfoProjection.getObservation()).orElse("-"));
    orderInfo.setStockType(Constant.StockType.getByCode(orderInfoProjection.getStockType()).getDescription());
    orderInfo.setServiceType(getServiceTypeDescription(Optional.ofNullable(orderInfoProjection.getServiceType()).orElse("-")));
    orderInfo.setPurchaseId(Optional.ofNullable(orderInfoProjection.getPurcharseId()).orElse("-"));
    orderInfo.setZoneId(orderInfoProjection.getZoneId());
    orderInfo.setOperator("-");

    return  orderInfo;
}

  private String getServiceTypeDescription(String serviceType) {
    List<String> services = Arrays.asList(serviceType.split("_"));
    if (CollectionUtils.isNotEmpty(services)) {
      if (services.stream().anyMatch(service -> service.equalsIgnoreCase("RET"))) {
        return "RET LITE";
      } else {
        if (services.get(0).equalsIgnoreCase("INKATRACKER") && services.get(1)
            .equalsIgnoreCase("LITE")) {
          return "Inkatracker Lite";
        } else {
          return "Inkatracker";
        }
      }
    }

    return StringUtils.EMPTY;
  }




}
