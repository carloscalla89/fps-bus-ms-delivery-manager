package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.canonical.manager.DetailProduct;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderInfo;
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
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    orderInfo.setScheduledTime(orderInfoProjection.getScheduledTime());
    orderInfo.setStatusName(orderInfoProjection.getStatusName());
    orderInfo.setServiceTypeShortCode(orderInfoProjection.getServiceTypeShortCode());
    orderInfo.setServiceChannel(orderInfoProjection.getServiceChannel());
    return orderInfo;
  }

  private OrderInfoProduct getOrderInfoProductByEcommerceId(long ecommerceId) {
    IOrderInfoProduct orderInfoProduct = orderRepository.getOrderInfoProductByEcommerceId(ecommerceId);
    List<IOrderInfoProductDetail> orderInfoProductDetail = orderRepository.getOrderInfoProductDetailByOrderFulfillmentId(ecommerceId);
    return getOrderInfoProduct(orderInfoProduct,orderInfoProductDetail);
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
      detailProduct.setShortDescription(orderDetailEntity.getShortDescription());
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

  private OrderInfoClient getOrderInfoClientByEcommerceId(long ecommerceId, OrderInfoConsolidated orderInfoConsolidated) {
    IOrderInfoClient orderInfoProjection = orderRepository.getOrderInfoClientByEcommercerId(ecommerceId);
    OrderInfoClient orderInfoDto = new OrderInfoClient();
    orderInfoDto.setAddressClient(orderInfoProjection.getAddressClient());
    orderInfoDto.setClientName(orderInfoProjection.getClientName());
    orderInfoDto.setCoordinates(orderInfoProjection.getCoordinates());
    orderInfoDto.setDocumentNumber(orderInfoProjection.getDocumentNumber());
    orderInfoDto.setEmail(orderInfoProjection.getEmail());
    orderInfoDto.setReference(orderInfoProjection.getReference());
    orderInfoDto.setPhone(orderInfoProjection.getPhone());
    orderInfoConsolidated.setOrderInfo(getOrderInfo(orderInfoProjection));
    return orderInfoDto;
  }


}
