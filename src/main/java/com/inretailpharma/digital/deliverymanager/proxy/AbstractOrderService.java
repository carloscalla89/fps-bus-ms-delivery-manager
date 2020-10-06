package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.AssignedOrdersCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;

import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import reactor.core.publisher.Mono;

import java.util.List;

public class AbstractOrderService implements OrderExternalService {

	@Override
	public Mono<Void> sendOrderReactive(OrderCanonical orderCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<Void> updateOrderReactive(OrderCanonical orderCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> getResultfromSellerExternalServices(OrderInfoCanonical orderInfoCanonical) {
		return null;
	}

	@Override
	public Mono<OrderCanonical> retrySellerCenterOrder(OrderDto orderDto) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> sendOrderToTracker(OrderCanonical orderCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> sendOrderEcommerce(IOrderFulfillment iOrderFulfillment,
												   List<IOrderItemFulfillment> itemFulfillments, String action,
												   StoreCenterCanonical storeCenterCanonical){
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<AssignedOrdersCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<String> unassignOrders(UnassignedCanonical unassignedCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<String> updateOrderStatus(Long ecommerceId, String status) {
		throw new UnsupportedOperationException();
	}

}
