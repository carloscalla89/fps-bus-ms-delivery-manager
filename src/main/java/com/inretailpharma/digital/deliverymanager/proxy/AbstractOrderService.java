package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.AssignedOrdersCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;

import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AbstractOrderService implements OrderExternalService {

	@Autowired
	private ObjectToMapper objectToMapper;

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
	public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
												   List<IOrderItemFulfillment> itemFulfillments,
												   StoreCenterCanonical storeCenterCanonical,
												   Long externalId, String statusDetail) {
		return null;
	}

	@Override
	public Mono<OrderCanonical> sendOrderToOrderTracker(OrderCanonical orderCanonical) {
		return null;
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

	@Override
	public Mono<com.inretailpharma.digital.deliverymanager.dto.OrderDto> getOrderFromEcommerce(Long ecommerceId) {
		return null;
	}

	protected ClientHttpConnector generateClientConnector(int connectionTimeOut, int readTimeOut) {

		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(tcpClient -> {
					tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeOut);
					tcpClient = tcpClient.doOnConnected(conn -> conn
							.addHandlerLast(
									new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS))
					);
					return tcpClient;
				});

		return new ReactorClientHttpConnector(httpClient);

	}

	protected Mono<OrderCanonical> mapResponseFromTracker(ClientResponse clientResponse, Long id, Long ecommerceId,
														  Long externalId) {
		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setId(id);
		orderCanonical.setEcommerceId(ecommerceId);
		orderCanonical.setExternalId(externalId);

		OrderStatusCanonical orderStatus;

		if (clientResponse.statusCode().is2xxSuccessful()) {

			orderCanonical.setTrackerId(ecommerceId);
			orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.CONFIRMED_TRACKER.name(), null);

			orderCanonical.setOrderStatus(orderStatus);

			return Mono.just(orderCanonical);
		} else {
			return clientResponse.body(BodyExtractors.toDataBuffers()).reduce(DataBuffer::write).map(dataBuffer -> {
				byte[] bytes = new byte[dataBuffer.readableByteCount()];
				dataBuffer.read(bytes);
				DataBufferUtils.release(dataBuffer);
				return bytes;
			})
					.defaultIfEmpty(new byte[0])
					.flatMap(bodyBytes -> Mono.error(new CustomException(clientResponse.statusCode().value()
							+":"+clientResponse.statusCode().getReasonPhrase()+":"+new String(bodyBytes),
							clientResponse.statusCode().value()))
					);

		}

	}

	protected Mono<OrderCanonical> mapResponseErrorFromTracker(Throwable e, Long id, Long ecommerceId, String statusCode) {

		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setEcommerceId(id);
		orderCanonical.setId(ecommerceId);

		OrderStatusCanonical orderStatus;

		if (statusCode.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.getCode())
				|| statusCode.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.getCode())) {

			orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.name(), e.getMessage());

		} else {

			orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.ERROR_INSERT_TRACKER.name(), e.getMessage());

		}

		orderCanonical.setOrderStatus(orderStatus);

		return Mono.just(orderCanonical);
	}


}
