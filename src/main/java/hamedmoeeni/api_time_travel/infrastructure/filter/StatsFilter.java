package hamedmoeeni.api_time_travel.infrastructure.filter;

import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;

@Component
public class StatsFilter implements GlobalFilter, Ordered {
    private static final String ATTRIBUTE_TIMESTAMP = "TIMESTAMP";
    private final Logger logger = LoggerFactory.getLogger(StatsFilter.class);
    @Getter
    @Setter
    private boolean storeBodies = false;
    @Getter
    @Setter
    private boolean bypassed = false;

    @Override
    public int getOrder() {
        //This filter needs to be before NettyWriteResponseFilter in chain
        return -2;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (bypassed) {
            return chain.filter(exchange);
        } else {
            return chain.filter(exchange.mutate().request(logRequestBody(exchange)).response(logResponseBody(exchange)).build());
        }
    }

    private ServerHttpRequestDecorator logRequestBody(ServerWebExchange exchange) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @SuppressWarnings("NullableProblems")
            @Override
            public Flux<DataBuffer> getBody() {
                if (storeBodies) {
                    return exchange.getRequest().getBody().buffer().map(dataBuffers -> {
                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer join = dataBufferFactory.join(dataBuffers);
                        byte[] content = new byte[join.readableByteCount()];
                        join.read(content);
                        DataBufferUtils.release(join);

                        String bodyStr = new String(content, StandardCharsets.UTF_8);
                        storeRequestDetails(exchange, bodyStr);
                        return new DefaultDataBufferFactory().wrap(content);
                    });
                } else {
                    storeRequestDetails(exchange);
                    return super.getBody();
                }
            }
        };
    }


    private ServerHttpResponseDecorator logResponseBody(final ServerWebExchange exchange) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            @SuppressWarnings("NullableProblems")
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (storeBodies) {
                    Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(flux.buffer().map(buffer -> {

                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer join = dataBufferFactory.join(buffer);
                        byte[] content = new byte[join.readableByteCount()];
                        join.read(content);
                        DataBufferUtils.release(join);

                        String bodyStr = new String(content, StandardCharsets.UTF_8);
                        storeResponseDetails(exchange, exchange.getResponse(), bodyStr);

                        getDelegate().getHeaders().setContentLength(bodyStr.getBytes().length);
                        return bufferFactory().wrap(bodyStr.getBytes());
                    }));
                } else {
                    storeResponseDetails(exchange, exchange.getResponse());
                    return super.writeWith(body);
                }
            }
        };
    }

    private void storeRequestDetails(ServerWebExchange exchange) {
        storeRequestDetails(exchange, null);
    }

    private void storeRequestDetails(ServerWebExchange exchange, String bodyStr) {
        //Store timestamp on exchange, it will be used to calculate duration when response is ready
        ZonedDateTime now = ZonedDateTime.now();
        exchange.getAttributes().put(ATTRIBUTE_TIMESTAMP, now);
        //Ids
        String requestId = exchange.getAttribute(ServerWebExchange.LOG_ID_ATTRIBUTE);
        String routeId = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ROUTE_ID_ATTR);
        //Request details
        ServerHttpRequest request = exchange.getRequest();
        String url = request.getURI().toString();
        String method = request.getMethod().toString();
        String headers = request.getHeaders().toString();
        //Store
        logger.info("request requestId: {}, routeId: {}, timeStamp: {},  url: {}, method: {}, headers: {}, body: {}",
                requestId, routeId, now, url, method, headers,
                bodyStr == null ? null : bodyStr.replace("\n", " "));
    }

    private void storeResponseDetails(ServerWebExchange exchange, ServerHttpResponse response) {
        storeResponseDetails(exchange, response, null);
    }

    private void storeResponseDetails(ServerWebExchange exchange, ServerHttpResponse response, String bodyStr) {
        //Id
        String requestId = exchange.getAttribute(ServerWebExchange.LOG_ID_ATTRIBUTE);
        //Response details
        String status = response.getStatusCode() == null ? null : response.getStatusCode().toString();
        String headers = response.getHeaders().toString();
        //Duration
        long duration = 0;
        Object requestTimeStamp = exchange.getAttribute(ATTRIBUTE_TIMESTAMP);
        if (requestTimeStamp instanceof ZonedDateTime) {
            duration = Duration.between((ZonedDateTime) requestTimeStamp, ZonedDateTime.now()).toMillis();
        }
        //Store
        logger.info("response requestId: {}, duration: {}, status: {}, headers: {}, body: {}",
                requestId, duration, status, headers,
                bodyStr == null ? null : bodyStr.replace("\n", " "));
    }
}
