package com.orderbook.rest.controller;

import com.orderbook.rest.service.OrderBookService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/order-book/")
public class OrderBookController {

   private final OrderBookService orderBookService;

   public OrderBookController(OrderBookService orderBookService) {
      this.orderBookService = orderBookService;
   }

   @GetMapping("/sell")
   public ResponseEntity<Publisher<String>> sell(@RequestParam String symbol, @RequestParam String amount, @RequestParam String limit,
                                                 @RequestParam(required = false) String rate) {
      if ( rate != null ) {
         log.info("Selling on symbol " + symbol + " with bids limit " + limit + " for amount " + amount + " with rate " + rate);
      } else {
         log.info("Selling on symbol " + symbol + " with bids limit " + limit + " for amount " + amount);
      }
      Mono<String> response = orderBookService.sellOrder(symbol, limit, amount, rate );
      return ResponseEntity.ok( response );
   }

   @GetMapping("/buy")
   public ResponseEntity<Publisher<String>> buy(@RequestParam String symbol, @RequestParam String amount, @RequestParam String limit) {

      log.info("Buying on symbol " + symbol + " with asks limit " + limit + " for amount " + amount);

      Mono<String> response = orderBookService.buyOrder(symbol, limit, amount );
      return ResponseEntity.ok( response );
   }

}
