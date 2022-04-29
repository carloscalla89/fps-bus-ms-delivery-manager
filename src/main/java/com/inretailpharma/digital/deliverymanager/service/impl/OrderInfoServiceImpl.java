package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrdersSelectedResponse;
import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.FilterOrderDTO;
import com.inretailpharma.digital.deliverymanager.dto.OderDetailOut;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService {

  private OrderRepository orderRepository;

  @Override
  public Mono<OrderInfoConsolidated> findOrderInfoClientByEcommerceId(long ecommerceId) {
    /*analisis del detalle*/
    OrderInfoConsolidated orderInfoConsolidated = new OrderInfoConsolidated();
    OrderInfoClient orderInfoClient = getOrderInfoClientByEcommerceId(ecommerceId,orderInfoConsolidated);
    OrderInfoPaymentMethodDto orderInfoPaymentMethod = getOrderInfoPaymentMethodByEcommercerId(ecommerceId);
    OrderInfoProduct orderInfoProduct = getOrderInfoProductByEcommerceId(ecommerceId);
    if(orderInfoProduct != null) {
      orderInfoProduct.setTotalDiscount(getOrderInfoProductDiscountByOrderId(orderInfoProduct.getId()));
    }
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
    orderInfo.setEcommerceIdCall(orderInfoProjection.getEcommerceIdCall());
    orderInfo.setLocalCode(orderInfoProjection.getLocalCode());
    orderInfo.setOrderType(orderInfoProjection.getOrderType());
    orderInfo.setScheduledTime(DateUtils.getLocalDateTimeWithFormatDDMMYY_AMPM(orderInfoProjection.getScheduledTime()));
    orderInfo.setStatusName(orderInfoProjection.getStatusName());
    orderInfo.setServiceTypeShortCode(DeliveryType.getByName(orderInfoProjection.getServiceTypeShortCode()).getDescription());
    orderInfo.setServiceChannel(orderInfoProjection.getServiceChannel());
    orderInfo.setSource(orderInfoProjection.getSource());
    orderInfo.setServiceType(getServiceType(orderInfoProjection.getServiceType()));
    return orderInfo;
  }

  private OrderHeaderDetail llenarDatosOrder(IOrderInfoClient item){
    OrderHeaderDetail order = new OrderHeaderDetail();
    order.setAddressClient(item.getAddressClient());
    order.setClientName(item.getClientName());
    order.setCoordinates(item.getCoordinates());
    order.setDocumentNumber(item.getDocumentNumber());
    order.setEmail(item.getEmail());
    order.setReference(Optional.ofNullable(item.getReference()).orElse("-"));
    order.setPhone(item.getPhone());
    order.setCompanyCode(item.getCompanyCode());
    order.setRuc(item.getRuc());
    order.setCompanyName(item.getCompanyName());
    order.setOrderId(item.getOrderId());
    order.setCompanyCode(item.getCompanyCode());
    order.setEcommerceId(item.getEcommerceId());
    order.setEcommerceIdCall(item.getEcommerceIdCall());
    order.setLocalCode(item.getLocalCode());
    order.setOrderType(item.getOrderType());

    order.setClient(item.getClientName());
    order.setDocumentoId(item.getDocumentNumber());
    order.setLocalId(item.getLocalCode());

    order.setScheduledTime(item.getScheduledTime());
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy hh:mm a");
    order.setPromiseDate(item.getScheduledTime().format(dtf));
    order.setStatusName(item.getStatusName());
    order.setStatusCode(item.getStatusCode());
    order.setOrderStatus(item.getStatusName());
    order.setServiceTypeShortCode(DeliveryType.getByName(item.getServiceTypeShortCode()).getDescription());
    order.setServiceChannel(item.getServiceChannel());
    order.setSource(item.getSource());
    //order.setServiceType(item.getServiceType());
    order.setServiceTypeId(item.getServiceTypeShortCode());
    //orderInfoConsolidated.setOrderInfo(getOrderInfo(orderInfoProjection));
    order.setCancelReason(Optional.ofNullable(item.getCancelReason()).orElse("-"));
    //order.setEcommerceId(orderInfoProjection.getEcommerceId());
    order.setLocalCode(Optional.ofNullable(item.getLocalCode()).orElse("-"));
    order.setObservation(Optional.ofNullable(item.getObservation()).orElse("-"));
    order.setStockType(Constant.StockType.getByCode(item.getStockType()).getDescription());
    order.setServiceType(Optional.ofNullable(item.getServiceType()).orElse("-"));
    //order.setServiceType(getServiceTypeDescription(Optional.ofNullable(item.getServiceType()).orElse("-")));
    order.setPurcharseId(Optional.ofNullable(item.getPurcharseId()).orElse("-"));
    order.setZoneId(item.getZoneId());
    order.setZoneId(Optional.ofNullable(item.getZoneId()).orElse("-"));
    return order;
  }

  @Override
  public OrdersSelectedResponse getOrderHeaderDetails(FilterOrderDTO filter) {
    log.info("[START ] getOrderHeaderDetails:{} ");
    log.info("[id Orders ] getOrderHeaderDetails:{} ",filter.getListOrderIds());
    List<IOrderInfoClient>  listOrders = orderRepository.getOrderHeaderDetails(filter.getListOrderIds());
    if(listOrders!=null){
      List<OrderHeaderDetail> orders = listOrders.stream().parallel().map(item -> {
        OrderHeaderDetail order = llenarDatosOrder(item);
        OrderInfoConsolidated consolidated=new OrderInfoConsolidated();
        consolidated.setOrderInfo(getOrderInfoDetail(order));
        consolidated.setOrderInfoAdditional(getOrderInfoAdd(order));
        OrderInfoClient orderInfoClient = getInfoCliente(order);
        OrderInfoPaymentMethodDto orderInfoPaymentMethod = getOrderInfoPaymentMethodByEcommercerId(order.getEcommerceId());
        OrderInfoProduct orderInfoProduct = getOrderInfoProductByEcommerceId(order.getEcommerceId());
        consolidated.setOrderInfoClient(orderInfoClient);
        consolidated.setPaymentMethodDto(orderInfoPaymentMethod);
        consolidated.setProductDetail(orderInfoProduct);
        OderDetailOut out=new OderDetailOut();
        out.setOrderInfoConsolidated(consolidated);
        order.setOderDetailOut(out);
        //log.info(" iterando :{} ",item.getEcommerceId());
        return order;
      }).collect(Collectors.toList());
      OrdersSelectedResponse response=new OrdersSelectedResponse();
      response.setOrders(orders);
      return response;
    }
    return null;
  }

  private OrderInfoClient getInfoCliente(OrderHeaderDetail orderInfoProjection) {
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
      //orderInfoConsolidated.setOrderInfo(getOrderInfoDetail(orderInfoProjection));
      //orderInfoConsolidated.setOrderInfoAdditional(getOrderInfoAdd(orderInfoProjection));
      return orderInfoDto;
  }

  private OrderInfo getOrderInfoDetail(OrderHeaderDetail orderInfoProjection) {
    OrderInfo orderInfo = new OrderInfo();
    orderInfo.setOrderId(orderInfoProjection.getOrderId());
    orderInfo.setCompanyCode(orderInfoProjection.getCompanyCode());
    orderInfo.setEcommerceId(orderInfoProjection.getEcommerceId());
    orderInfo.setEcommerceIdCall(orderInfoProjection.getEcommerceIdCall());
    orderInfo.setLocalCode(orderInfoProjection.getLocalCode());
    orderInfo.setOrderType(orderInfoProjection.getOrderType());
    orderInfo.setScheduledTime(DateUtils.getLocalDateTimeWithFormatDDMMYY_AMPM(orderInfoProjection.getScheduledTime()));
    orderInfo.setStatusName(orderInfoProjection.getStatusName());
    orderInfo.setServiceTypeShortCode(DeliveryType.getByName(orderInfoProjection.getServiceTypeShortCode()).getDescription());
    orderInfo.setServiceChannel(orderInfoProjection.getServiceChannel());
    orderInfo.setSource(orderInfoProjection.getSource());
    orderInfo.setServiceType(getServiceType(orderInfoProjection.getServiceType()));
    return orderInfo;
  }

  private OrderInfoAdditional getOrderInfoAdd(OrderHeaderDetail orderInfoProjection) {
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

  private String getServiceType(String serviceType) {
    log.info("Order Service Type: {} "+serviceType);
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

  private BigDecimal getOrderInfoProductDiscountByOrderId(BigInteger orderId) {
    BigDecimal totalDiscount = orderRepository.getOrderInfoProductDiscountByOrderId(orderId);
    if (totalDiscount != null) {
      return totalDiscount;
    }
    return new BigDecimal(0);
  }

  private OrderInfoProduct getOrderInfoProduct(IOrderInfoProduct orderInfoProduct, List<IOrderInfoProductDetail> orderInfoProductDetail) {
    OrderInfoProduct orderInfo = new OrderInfoProduct();
    orderInfo.setId(orderInfoProduct.getId());
    orderInfo.setDeliveryAmount(orderInfoProduct.getDeliveryAmount());
    //orderInfo.setTotalDiscount(orderInfoProduct.getTotalDiscount());
    orderInfo.setTotalImport(orderInfoProduct.getTotalImport());
    orderInfo.setTotalImportTOH(orderInfoProduct.getTotalImportTOH());
    orderInfo.setTotalImportWithOutDiscount(orderInfoProduct.getTotalImportWithOutDiscount());
    List<DetailProduct> detail = orderInfoProductDetail.stream().map(orderDetailEntity -> {
      DetailProduct detailProduct = new DetailProduct();
      detailProduct.setName(orderDetailEntity.getName());
      detailProduct.setQuantity(orderDetailEntity.getQuantity());
      //TODO: OMS
      detailProduct.setPresentationDescription(orderDetailEntity.getPresentationDescription());
      detailProduct.setSku(orderDetailEntity.getSku());
      detailProduct.setTotalPrice(orderDetailEntity.getTotalPrice());
      detailProduct.setTotalPriceAllPaymentMethod(orderDetailEntity.getTotalPriceAllPaymentMethod());
      detailProduct.setTotalPriceTOH(orderDetailEntity.getTotalPriceTOH());
      detailProduct.setUnitPrice(orderDetailEntity.getUnitPrice());
      return detailProduct;
    }).collect(Collectors.toList());

    orderInfo.setProducts(detail);

    return orderInfo;

  }

  private OrderInfoPaymentMethodDto getOrderInfoPaymentMethodByEcommercerId(long ecommerceId) {
    IOrderInfoPaymentMethod orderInfoProjection = orderRepository.getInfoPaymentMethod(ecommerceId);
    if(orderInfoProjection!=null){
      OrderInfoPaymentMethodDto orderInfoDto  = new OrderInfoPaymentMethodDto();
      orderInfoDto.setCardBrand(orderInfoProjection.getCardBrand());
      orderInfoDto.setLiquidationStatus(Optional.ofNullable(orderInfoProjection.getLiquidacionStatus()).orElse("-"));
      orderInfoDto.setCardNumber(orderInfoProjection.getCardNumber());
      orderInfoDto.setChangeAmount(orderInfoProjection.getChangeAmount());
      orderInfoDto.setCodAuthorization(orderInfoProjection.getCodAuthorization());
      orderInfoDto.setFinancial(orderInfoProjection.getFinancial());
      orderInfoDto.setPaymentGateway(orderInfoProjection.getPaymentGateway());
      orderInfoDto.setServiceTypeCode(orderInfoProjection.getServiceTypeCode());
      orderInfoDto.setPaymentType(Constant.PaymentType.getByCode(orderInfoProjection.getPaymentType()).getPaymentTypeDescription());
      orderInfoDto.setPaymentDate(orderInfoProjection.getDateConfirmed());
      orderInfoDto.setTransactionId(orderInfoProjection.getTransactionId());
      return orderInfoDto;
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
