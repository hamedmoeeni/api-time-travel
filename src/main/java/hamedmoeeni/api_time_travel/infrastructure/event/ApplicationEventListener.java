package hamedmoeeni.api_time_travel.infrastructure.event;

import hamedmoeeni.api_time_travel.usecase.routing.RoutingAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesResultEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RoutingAdapter routingAdapter;
    private boolean initiallyLoadedRoutes = false;

    public ApplicationEventListener(RoutingAdapter routingAdapter) {
        this.routingAdapter = routingAdapter;
    }

    @EventListener(RefreshRoutesResultEvent.class)
    public void onRefreshRoutesResultEvent(RefreshRoutesResultEvent event) {
        if (event.isSuccess()) {
            if (!initiallyLoadedRoutes) {
                initiallyLoadedRoutes = true;
                routingAdapter.initiallyLoadRoutes()
                        .doOnSuccess(_->logger.info("Initially loaded routes"))
                        .subscribe();
            } else {
                logger.info("Refresh routes successful");
            }
        } else {
            logger.error("Refresh routes failed", event.getThrowable());
        }
    }
}
