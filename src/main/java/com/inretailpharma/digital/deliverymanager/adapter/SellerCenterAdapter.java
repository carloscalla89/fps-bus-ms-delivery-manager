package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalSellerCenterProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerCenterAdapter extends AdapterAbstractUtil implements ISellerCenterAdapter {

	private OrderExternalService sellerCenterExternalService;

	@Autowired
	public SellerCenterAdapter(@Qualifier("sellerCenterService") OrderExternalService sellerCenterExternalService) {
		this.sellerCenterExternalService = sellerCenterExternalService;
	}

	@Override
	public Mono<OrderCanonical> updateStatusOrderSeller(Long ecommerceId, String actionName) {
		log.info("[START] updateStatusOrderSeller");
		Constant.OrderStatusTracker orderStatusTracker = Constant.OrderStatusTracker.getByActionName(actionName);

		String statusToSend = orderStatusTracker.getTrackerLiteStatus();

		sellerCenterExternalService.updateStatusOrderSeller(ecommerceId, statusToSend).subscribe();

		OrderCanonical orderCanonical = new OrderCanonical();
		orderCanonical.setEcommerceId(ecommerceId);
		orderCanonical.setTarget(Constant.TARGET_SELLER);

		OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
		orderStatusCanonical.setCode(orderStatusTracker.getOrderStatus().getCode());
		orderStatusCanonical.setName(orderStatusTracker.getOrderStatus().name());

		orderCanonical.setOrderStatus(orderStatusCanonical);
		log.info("[END] updateStatusOrderSeller");
		return Mono.just(orderCanonical);

	}

	public Mono<OrderCanonical> updateListStatusOrderSeller(Long ecommerceId, List<HistorySynchronizedDto> history) {
		String listStatus = "";
		Constant.OrderStatusTracker orderStatusTracker = null;
		OrderCanonical orderCanonical = new OrderCanonical();

		for (HistorySynchronizedDto historyStatus : history) {
			orderStatusTracker = Constant.OrderStatusTracker.getByActionName(historyStatus.getAction());
			String statusToSend = orderStatusTracker.getTrackerLiteStatus();

			if (Constant.ActionOrder.DELIVER_ORDER.name().equalsIgnoreCase(historyStatus.getAction())
					|| Constant.ActionOrder.ON_ROUTE_ORDER.name().equalsIgnoreCase(historyStatus.getAction())) {
				listStatus = listStatus + statusToSend + ",";
			}
		}

		if (!listStatus.isEmpty()) {
			log.info("[START] updateListStatusOrderSeller");
			sellerCenterExternalService.updateStatusOrderSeller(ecommerceId, listStatus).subscribe();
			log.info("[END] updateListStatusOrderSeller");
		}

		return Mono.just(orderCanonical);

	}

	protected Mono<OrderCanonical> getDataToSentAudit(OrderCanonical orderCanonical, ActionDto actionDto) {

		log.info("order:{}, target:{}, action:{}", orderCanonical.getEcommerceId(), orderCanonical.getTarget(),
				actionDto);

		LocalDateTime localDateTime = DateUtils.getLocalDateTimeObjectNow();
		orderCanonical.setUpdateBy(actionDto.getUpdatedBy());
		orderCanonical.setSource(actionDto.getOrigin());
		orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

		orderCanonical.getOrderStatus().setCancellationCode(actionDto.getOrderCancelCode());
		orderCanonical.getOrderStatus().setCancellationObservation(actionDto.getOrderCancelObservation());

		return Mono.just(orderCanonical);

	}
}
