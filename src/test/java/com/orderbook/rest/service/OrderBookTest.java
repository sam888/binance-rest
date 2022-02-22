package com.orderbook.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderbook.rest.client.OrderBookClient;
import com.orderbook.rest.dto.OrderBook;
import com.orderbook.rest.util.ResourcesDataUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Profile("dev")
public class OrderBookTest {

   @Autowired
   private OrderBookClient orderBookClient;

   @Autowired
   private OrderBookService orderBookService;

   @Test
   public void test_orderBookMapping_by_objectMapper_success() throws  Exception {
      OrderBook orderBook = getOrderBookByObjectMapper();
      assertNotNull( orderBook );
   }

   /*
   @Test
   public void test_orderBookService_success() throws Exception {
      OrderBook orderBook = getOrderBookByObjectMapper();

      // Sell 100 coins to available bids from Order Book for another coin
      BigDecimal coinsAcquiredByTrade = orderBookService.fillSellOrder( BigDecimal.valueOf(100.00), orderBook.getBids());
      System.out.println("Coins acquired by sell order: " + coinsAcquiredByTrade);
   }


   @Test
   public void test_orderBookClient() throws  Exception {
      orderBookClient = new OrderBookClient();
      Mono<OrderBook> orderBook  = orderBookClient.getOrderBook("BTCUSDT", "10");
      orderBook.map( orderBook1 -> {
         System.out.println("OrderBook: " + orderBook1);
         return orderBook1;
      });
   }
   */


   private OrderBook getOrderBookByObjectMapper() throws Exception {

      // Test data obtained from https://api.binance.com/api/v3/depth?limit=10&symbol=HOTBUSD
      String data = ResourcesDataUtil.getFileAsString("data", "data.json");
      assertNotNull( data );
      System.out.println("data: " + data );

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(data, OrderBook.class);
   }
}
