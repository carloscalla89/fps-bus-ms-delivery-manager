package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.InsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.ResponseDispatcherCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.StatusDispatcher;
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
import com.inretailpharma.digital.deliverymanager.errorhandling.ResponseErrorGeneric;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
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
import java.util.Optional;
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
												   Long externalId, String statusDetail,String statusName,
												   String orderCancelCode, String orderCancelObservation) {
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

	protected ClientHttpConnector generateClientConnector(int connectionTimeOut, long readTimeOut) {
		log.info("generateClientConnector, connectionTimeOut:{}, readTimeOut:{}",connectionTimeOut,readTimeOut);
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

	protected Mono<OrderCanonical> mapResponseFromDispatcher(ClientResponse clientResponse, Long ecommerceId, String companyCode) {

		if (clientResponse.statusCode().is2xxSuccessful()) {

			return clientResponse
					.bodyToMono(ResponseDispatcherCanonical.class)
					.flatMap(cr -> {
						InsinkResponseCanonical dispatcherResponse = cr.getBody();
						StatusDispatcher statusDispatcher = cr.getStatus();

						log.info("result dispatcher to reattempt - body:{}, status:{}",dispatcherResponse, statusDispatcher);

						OrderStatusCanonical orderStatus = new OrderStatusCanonical();
						Constant.OrderStatus orderStatusUtil = Constant
								.OrderStatus
								.getByName(Constant.StatusDispatcherResult.getByName(statusDispatcher.getCode()).getStatus());

						orderStatus.setCode(orderStatusUtil.getCode());
						orderStatus.setName(orderStatusUtil.name());
						orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

						if (!statusDispatcher.isSuccessProcess()) {
							String stringBuffer = "code error:" +
									dispatcherResponse.getErrorCode() +
									", description:" +
									statusDispatcher.getDescription() +
									", detail:" +
									dispatcherResponse.getMessageDetail();

							orderStatus.setDetail(stringBuffer);
						}

						OrderCanonical resultCanonical = new OrderCanonical();
						resultCanonical.setEcommerceId(ecommerceId);
						resultCanonical.setExternalId(
								Optional
										.ofNullable(dispatcherResponse.getInkaventaId())
										.map(Long::parseLong).orElse(null)
						);
						resultCanonical.setCompanyCode(companyCode);
						resultCanonical.setOrderStatus(orderStatus);

						return Mono.just(resultCanonical);


					});

		} else {
			ResponseErrorGeneric<OrderCanonical> responseErrorGeneric = new ResponseErrorGeneric<>();

			return responseErrorGeneric.getErrorFromClientResponse(clientResponse);
		}

	}


	protected Mono<OrderCanonical> mapResponseFromTracker(ClientResponse clientResponse, Long id, Long ecommerceId,
														  Long externalId, String statusName) {
		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setId(id);
		orderCanonical.setEcommerceId(ecommerceId);
		orderCanonical.setExternalId(externalId);

		OrderStatusCanonical orderStatus;

		if (clientResponse.statusCode().is2xxSuccessful()) {

			orderCanonical.setTrackerId(ecommerceId);

			if (statusName.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.name())
					|| statusName.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.name())
					|| statusName.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER_NOT_ENOUGH_STOCK.name())
					|| statusName.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK.name())) {

				orderStatus = objectToMapper.getOrderStatusInkatracker(statusName, null);
			} else {
				orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.CONFIRMED_TRACKER.name(), null);
			}



			orderCanonical.setOrderStatus(orderStatus);

			return Mono.just(orderCanonical);
		} else {

			ResponseErrorGeneric<OrderCanonical> responseErrorGeneric = new ResponseErrorGeneric<>();

			return responseErrorGeneric.getErrorFromClientResponse(clientResponse);

		}

	}

	protected Mono<OrderCanonical> mapResponseErrorFromTracker(Throwable e, Long id, Long ecommerceId, String statusName) {

		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setId(id);
		orderCanonical.setEcommerceId(ecommerceId);

		OrderStatusCanonical orderStatus;

		if (statusName.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.name())
				|| statusName.equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.name())) {

			orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.name(), e.getMessage());

		} else {

			orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.ERROR_INSERT_TRACKER.name(), e.getMessage());

		}

		orderCanonical.setOrderStatus(orderStatus);

		return Mono.just(orderCanonical);
	}

	protected Mono<OrderCanonical> mapResponseErrorFromDispatcher(Throwable e, Long ecommerceId) {

		OrderCanonical orderCanonical = new OrderCanonical();

		orderCanonical.setEcommerceId(ecommerceId);
		OrderStatusCanonical orderStatus = new OrderStatusCanonical();

		orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
		orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
		orderStatus.setDetail(e.getMessage());
		orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

		orderCanonical.setOrderStatus(orderStatus);

		return Mono.just(orderCanonical);
	}


}
