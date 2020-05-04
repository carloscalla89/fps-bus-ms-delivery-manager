package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TrackerFacade {

    public Mono<OrderTrackerResponseCanonical> assignShipper(ProjectedGroupCanonical projectedGroupCanonical) {
        return Mono.just(new OrderTrackerResponseCanonical());
    }
}
