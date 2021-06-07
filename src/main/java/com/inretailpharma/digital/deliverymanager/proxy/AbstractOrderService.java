package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.AssignedOrdersCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.LiquidationDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.dto.notification.MessageDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.ResponseErrorGeneric;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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
	public Mono<Void> updateOrderReactive(OrderCanonical orderCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company,
															  String serviceType, String cancelDescription) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<Void> sendOrderReactive(OrderCanonical orderAuditCanonical) {
		return null;
	}

	@Override
	public Mono<Void> sendNotification(MessageDto messageDto) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
												   List<IOrderItemFulfillment> itemFulfillments,
												   StoreCenterCanonical storeCenterCanonical,
												   Long externalId, String statusDetail,String statusName,
												   String orderCancelCode, String orderCancelDescription,
												   String orderCancelObservation) {
		return null;
	}

	@Override
	public Mono<OrderCanonical> createOrderToLiquidation(LiquidationDto liquidationDto) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> updateOrderToLiquidation(String ecommerceId, StatusDto statusDto) {
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
	public Mono<OrderCanonical> updateOrderStatus(Long ecommerceId,  ActionDto actionDto) {
		return null;
	}

	public Mono<OrderCanonical> getResultfromOnlinePaymentExternalServices(Long ecommercePurchaseId, String source,
																		   String serviceTypeShortCode,
																		   String companyCode, ActionDto actionDto) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Mono<String> addControversy(ControversyRequestDto controversyRequestDto, Long EcommerceId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<OrderCanonical> sendOrderToOrderTracker(OrderCanonical orderCanonical, ActionDto actionDto) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<Void> createOrderNewAudit(AuditHistoryDto auditHistoryDto) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<Void> updateOrderNewAudit(AuditHistoryDto orderCanonical) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode) {
		throw new UnsupportedOperationException();
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


	protected Mono<OrderCanonical> mapResponseFromTracker(ClientResponse clientResponse, Long id, Long ecommerceId,
														  Long externalId, String statusName, String cancellationCode,
														  String cancellationObservation, String statusDetail) {

		if (clientResponse.statusCode().is2xxSuccessful()) {

            return clientResponse
                        .bodyToMono(Void.class)
                        .thenReturn((getResponse(id, ecommerceId, externalId, statusName, cancellationCode,
								cancellationObservation, statusDetail)));

		} else {
            log.error("Error in response from tracker, ecommerceId:{}, statusCode:{}",
					ecommerceId,clientResponse.statusCode());
			ResponseErrorGeneric<OrderCanonical> responseErrorGeneric = new ResponseErrorGeneric<>();

			return responseErrorGeneric.getErrorFromClientResponse(clientResponse);

		}

	}

	protected Mono<OrderCanonical> mapResponseFromTargetLiquidation(ClientResponse clientResponse, Long ecommerceId,
																	StatusDto statusDto) {

		if (clientResponse.statusCode().is2xxSuccessful()) {

			return clientResponse
					.bodyToMono(Void.class)
					.thenReturn((getResponseLiquidation(ecommerceId, statusDto, null, true)));

		} else {
			log.error("Error in response from liquidation, ecommerceId:{}, statusCode:{}",
					ecommerceId,clientResponse.statusCode());
			ResponseErrorGeneric<OrderCanonical> responseErrorGeneric = new ResponseErrorGeneric<>();

			return responseErrorGeneric.getErrorFromClientResponse(clientResponse);

		}

	}

	protected Mono<OrderCanonical> mapResponseFromTargetWithErrorOrEmpty(Long ecommerceId, String code, String statusDetail) {

		Constant.LiquidationStatus StatusLiquidationError = Constant.LiquidationStatus.getStatusByCode(code);
		StatusDto statusDto = new StatusDto();
		statusDto.setCode(StatusLiquidationError.getCode());
		statusDto.setName(StatusLiquidationError.name());

		return Mono.just(getResponseLiquidation(ecommerceId, statusDto, statusDetail, false));

	}

	protected Mono<OrderCanonical> mapResponseFromUpdateTracker(ClientResponse clientResponse, Long ecommerceId,
																Constant.OrderStatusTracker orderStatusInkatracker) {


		if (clientResponse.statusCode().is2xxSuccessful()) {

			return clientResponse
					.bodyToMono(Void.class)
					.thenReturn((getResponse(orderStatusInkatracker)));

		} else {
			log.error("Error in response from Updatetracker, ecommerceId:{}, statusCode:{}"
					,ecommerceId,clientResponse.statusCode());

			ResponseErrorGeneric<OrderCanonical> responseErrorGeneric = new ResponseErrorGeneric<>();

			return responseErrorGeneric.getErrorFromClientResponse(clientResponse);


		}

	}


	private OrderCanonical getResponse(Constant.OrderStatusTracker orderStatusInkatracker) {
		OrderCanonical orderCanonical = new OrderCanonical();

		Constant.OrderStatus orderStatusResult = orderStatusInkatracker.getOrderStatus();

		OrderStatusCanonical orderStatus = new OrderStatusCanonical();
		orderStatus.setCode(orderStatusResult.getCode());
		orderStatus.setName(orderStatusResult.name());
		orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
		orderStatus.setSuccessful(orderStatusResult.isSuccess());
		orderCanonical.setOrderStatus(orderStatus);

		return orderCanonical;
	}

	private OrderCanonical getResponse(Long id, Long ecommerceId,
                                       Long externalId, String statusName, String cancellationCode,
									   String cancellationObservation, String statusDetail) {
        OrderCanonical orderCanonical = new OrderCanonical();
        orderCanonical.setId(id);
        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setExternalId(externalId);
		orderCanonical.setTrackerId(externalId);

        orderCanonical.setOrderStatus(
        		objectToMapper.getOrderStatus(statusName, statusDetail, cancellationCode, cancellationObservation)
		);

        return orderCanonical;
    }

	private OrderCanonical getResponseLiquidation(Long ecommerceId, StatusDto statusDto, String statusDetail,
												  boolean isSuccess) {
		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setEcommerceId(ecommerceId);
		orderCanonical.setOrderStatus(objectToMapper.getOrderStatusLiquidation(statusDto, statusDetail, isSuccess));

		return orderCanonical;
	}

	protected Mono<OrderCanonical> mapResponseErrorFromTracker(Throwable e, Long id, Long ecommerceId, String statusName,
															   String cancellationCode, String cancellationObservation) {

		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setId(id);
		orderCanonical.setEcommerceId(ecommerceId);

		OrderStatusCanonical orderStatus = objectToMapper
												.getOrderStatus(statusName, e.getMessage(), cancellationCode, cancellationObservation);

		orderCanonical.setOrderStatus(orderStatus);

		return Mono.just(orderCanonical);
	}

	protected Mono<OrderCanonical> mapResponseErrorWhenTheOrderIsCreated(Throwable e, Long id, Long ecommerceId,
																		 String statusName, String cancellationCode,
																		 String cancellationObservation) {

		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setId(id);
		orderCanonical.setEcommerceId(ecommerceId);

		String status;

		if (cancellationCode != null) {
			status = Constant.OrderStatus.ERROR_CANCELLED.name();
		} else {
			status = Constant.OrderStatus.ERROR_INSERT_TRACKER.name();
		}

		OrderStatusCanonical orderStatus = objectToMapper
				.getOrderStatus(status, e.getMessage(), cancellationCode, cancellationObservation);

		orderCanonical.setOrderStatus(orderStatus);

		return Mono.just(orderCanonical);
	}


}
