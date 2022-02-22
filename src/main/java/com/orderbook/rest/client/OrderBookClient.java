package com.orderbook.rest.client;

import com.orderbook.rest.dto.OrderBook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class OrderBookClient extends BaseClient {

   @Value("${binance.order-book.url}")
   private String binanceOrderBookUrl;

   public Mono<OrderBook> getOrderBook(String symbol, String limit) {
      String requestUrl = binanceOrderBookUrl + "?limit=" + limit + "&symbol=" + symbol;

      log.info("Getting binance Order Book data with url: " + requestUrl);
      return getClient( requestUrl ).get()
         .retrieve().bodyToMono( OrderBook.class );
   }

   // Alternative way of calling Binance API by using synchronous ResTemplate
   public OrderBook getOrderBook2(String symbol, String limit)  {
      String requestUrl = binanceOrderBookUrl + "?limit=" + limit + "&symbol=" + symbol;

      RestTemplate restTemplate = new RestTemplate();
      log.info("Getting binance Order Book data with url: " + requestUrl);

      OrderBook orderBook = restTemplate.getForObject( requestUrl, OrderBook.class);
      // log.info( "orderBook: " + orderBook);
      return orderBook;
   }


}
