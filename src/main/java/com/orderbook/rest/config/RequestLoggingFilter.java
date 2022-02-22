package com.orderbook.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

/**
 * See https://piotrminkowski.com/2019/10/15/reactive-logging-with-spring-webflux-and-logstash/ for how these reactive
 * logging works with Spring WebFlux.
 *
 */
@Slf4j
@Configuration
@Order(5)
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();

        log.info("Request: path={}", request.getPath());
        ServerWebExchangeDecorator decorator =
                new ServerWebExchangeDecorator(serverWebExchange) {

                    @Override
                    public ServerHttpResponse getResponse() {
                        return new ResponseLoggingDecorator(serverWebExchange.getResponse());
                    }

                    @Override
                    public ServerHttpRequest getRequest() {
                        return new RequestLoggingDecorator(serverWebExchange.getRequest());
                    }
                };

        return webFilterChain.filter(decorator);
    }
}

@Slf4j
class RequestLoggingDecorator extends ServerHttpRequestDecorator {

    public RequestLoggingDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }


    @Override
    public Flux<DataBuffer> getBody() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return super.getBody().doOnNext(dataBuffer -> {
            try {
                Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                String body = baos.toString(StandardCharsets.UTF_8);
                log.info("Request: payload={}", body);
            } catch (IOException e) {
                log.error(e.getMessage());
            } finally {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        });
    }
}

@Slf4j
class ResponseLoggingDecorator extends ServerHttpResponseDecorator {
    public ResponseLoggingDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        Mono<DataBuffer> buffer = Mono.from(body);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return super.writeWith(buffer.doOnNext(dataBuffer -> {
            try {
                Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                String resp = baos.toString(StandardCharsets.UTF_8);
                log.info("Response: payload={}", resp);
            } catch (IOException e) {
                log.error(e.getMessage());
            } finally {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }));
    }
}
