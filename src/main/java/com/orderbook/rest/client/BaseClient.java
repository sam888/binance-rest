package com.orderbook.rest.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.orderbook.rest.dto.BaseResponse;
import com.orderbook.rest.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
public class BaseClient {

    protected WebClient getClient(String baseUrl, String apiUri){
        return WebClient.builder()
                .filter( errorHandlingFilter( apiUri ) )
                .exchangeStrategies(getExchangeStrategies())
                .baseUrl( baseUrl ).build();
    }

    protected WebClient getClient(String baseUrl){
        return WebClient.builder()
                .filter( errorHandlingFilter( baseUrl ) )
                .exchangeStrategies(getExchangeStrategies())
                .baseUrl( baseUrl ).build();
    }


    protected ExchangeStrategies getExchangeStrategies(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(MapperFeature.USE_ANNOTATIONS);

        return ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                }).build();
    }

    protected static ExchangeFilterFunction errorHandlingFilter(String apiUri) {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            HttpStatus status = clientResponse.statusCode();
            if (status.is2xxSuccessful()){
                return Mono.just(clientResponse);
            }else {
                return clientResponse.bodyToMono(BaseResponse.class)
                        .flatMap(errorBody -> Mono.error(new InternalException(errorBody, apiUri)));
            }
        });
    }

    /*
    protected ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }
    */

}
