package com.inretailpharma.digital.deliverymanager.adapter;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ProductDimensionDto;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.proxy.ProductService;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.ObjectUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RoutingAdapter extends AdapterAbstractUtil implements IRoutingAdapter {
	
	private OrderExternalService routingService;
	private OrderExternalService auditService;
	private ObjectToMapper objectToMapper;
	private ApplicationParameterService applicationParameterService;
	private ProductService productService;
	
	public RoutingAdapter(@Qualifier("routing")OrderExternalService routingService,
			@Qualifier("audit") OrderExternalService auditService,
			ObjectToMapper objectToMapper,
			ApplicationParameterService applicationParameterService,
			ProductService productService) {
		this.routingService = routingService;
		this.auditService = auditService;
		this.objectToMapper = objectToMapper;
		this.applicationParameterService = applicationParameterService;
		this.productService = productService;
	}

	@Override
	public Mono<OrderCanonical> createOrder(OrderCanonical orderCanonical) {
		
		log.info("[INFO] RoutingAdapter.createOrder - orderCanonical: {}", ObjectUtil.objectToJson(orderCanonical));
		
		Optional.ofNullable(orderCanonical.getStoreCenter())
			.ifPresent(sc -> {
				
				Optional.ofNullable(orderCanonical.getOrderDetail())
				.ifPresent(od -> {
					
					if (sc.isExternalRoutingEnabled() && Constant.DELIVERY.equalsIgnoreCase(od.getServiceType())) {
						
						boolean routingEnabled = Optional
			                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ENABLED_EXTERNAL_ROUTING))
			                    .map(val -> Constant.Logical.getByValueString(val.getValue()).value())
			                    .orElse(false);
						
						if (routingEnabled) {
							
							BigDecimal defaultVolume = Optional
				                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ROUTING_DEFAULT_VOLUME))
				                    .map(val -> new BigDecimal(val.getValue()))
				                    .orElse(Constant.Routing.DEFAULT_VOLUME);
							
							int defaultDeliveryTime = Optional
				                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ROUTING_DEFAULT_DELIVERY_TIME))
				                    .map(val -> Integer.parseInt(val.getValue()))
				                    .orElse(Constant.Routing.DEFAULT_DELIVERY_TIME);
							
							List<String> productCodes = orderCanonical.getOrderItems()
									.stream()
									.filter(f -> !Constant.DELIVERY_CODE.equals(f.getProductCode()))
									.map(OrderItemCanonical::getProductCode).collect(Collectors.toList());
							
							productService.getDimensions(productCodes)
								.buffer()
								.flatMap(m -> {
									
									log.info("[INFO] RoutingAdapter.createOrder - productCodes: {}", m);								
									Map<String, ProductDimensionDto> data = objectToMapper.convertProductDimensionsDtoToMap(m);
									
									return routingService.createOrderRouting(orderCanonical.getEcommerceId(),
											objectToMapper.convertCanonicalToRoutedOrder(orderCanonical, orderCanonical.getOrderItems(),
													data, defaultVolume, defaultDeliveryTime, sc.getExternalRoutingLocalCode()))
									.flatMap(a -> {
										
										a.setTarget(Constant.TARGET_ROUTING);						
										return auditService.updateOrderNewAudit(getAuditHistoryDtoFromObject(a, null));
										
									});

								}).subscribe();	
							
						}					
						
					}
					
				});
			});	

		return Mono.just(orderCanonical);
	}

	@Override
	public Mono<OrderCanonical> cancelOrder(Long orderId, boolean externalRouting, String serviceType, String action, String origin) {
		
		Optional.ofNullable(orderId)
			.ifPresent(sc -> {
				
				if (externalRouting &&
						Constant.DELIVERY.equalsIgnoreCase(serviceType) &&
						(Constant.ActionOrder.CANCEL_ORDER.name().equals(action) || Constant.ActionOrder.REJECT_ORDER.name().equals(action)) &&
						!Constant.ORIGIN_ROUTING.equalsIgnoreCase(origin)
						) {
					
					boolean routingEnabled = Optional
		                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ENABLED_EXTERNAL_ROUTING))
		                    .map(val -> Constant.Logical.getByValueString(val.getValue()).value())
		                    .orElse(false);
					
					if (routingEnabled) {
						
						routingService.updateOrderRouting(orderId)
						.flatMap(a -> {
							
							a.setTarget(Constant.TARGET_ROUTING);						
							return auditService.updateOrderNewAudit(getAuditHistoryDtoFromObject(a, null));
							
						})
						.subscribe();	
						
					}
				}
			});			
		
		return Mono.just(new OrderCanonical());
	}

}
