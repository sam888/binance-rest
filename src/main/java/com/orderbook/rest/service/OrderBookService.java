package com.orderbook.rest.service;

import com.orderbook.rest.client.OrderBookClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class OrderBookService {

   private final OrderBookClient orderBookClient;
   private static final DecimalFormat currencyFormatter = new DecimalFormat("$#,###,###.###");
   private static final DecimalFormat coinAmountFormatter = new DecimalFormat("###,###,###,###,###.######");

   public OrderBookService(OrderBookClient orderBookClient) {
      this.orderBookClient = orderBookClient;
   }

   // Return estimate profit or coins traded
   public Mono<String> sellOrder(String symbol, String limit, String amountToSell, String rate) {
      return orderBookClient.getOrderBook(symbol, limit).flatMap( orderBook -> {
         BigDecimal coinsAcquired = fillSellOrder( new BigDecimal(amountToSell), orderBook.getBids());
         coinsAcquired.setScale(6, RoundingMode.HALF_UP);

         String returnValue = null;
         if ( rate != null ) {
            BigDecimal fiatValue = coinsAcquired.multiply( new BigDecimal( rate ));
            returnValue = currencyFormatter.format( fiatValue ); // return profit of trade
         } else {
            //returnValue = String.format("%.6f", coinsAcquired ); // return amount of coins acquired by trade
            returnValue = coinAmountFormatter.format( coinsAcquired ); // return amount of coins acquired by trade
         }
         return Mono.just( returnValue );
      });
   }

   public Mono<String> buyOrder(String symbol, String limit, String amountToBuy) {
      return orderBookClient.getOrderBook(symbol, limit).flatMap( orderBook -> {
         BigDecimal coinsBought = fillBuyOrder( new BigDecimal( amountToBuy ), orderBook.getAsks());
         coinsBought.setScale(6, RoundingMode.HALF_UP);

         String returnValue = returnValue = coinAmountFormatter.format( coinsBought ); // return amount of coins acquired by trade
         return Mono.just( returnValue );
      });
   }

   /**
    * Fill Sell order using bids data from order book.
    *
    * @param sellAmount the amount of coin to sell to buy orders
    * @param bids       an array of buy limit orders from order book where each buy order is [bidPrice, bidAmount] so the array
    *                   will be like [ [bidPrice1, bidAmount1], [bidPrice2, bidAmount2], ... ]
    * @return
    */
   public BigDecimal fillSellOrder(BigDecimal sellAmount, String[][] bids) {

      BigDecimal bidPrice = null;
      BigDecimal bidAmount = null;
      BigDecimal sellProfit = BigDecimal.ZERO;
      List<String[]> bidsList = Arrays.asList( bids );
      for (String[] bid : bidsList) {
         bidPrice = new BigDecimal(bid[0]);
         bidAmount = new BigDecimal(bid[1]);

         if ( sellAmount.compareTo( bidAmount ) > 0 ) {
            sellProfit = sellProfit.add( bidAmount.multiply( bidPrice ) );
            sellAmount = sellAmount.subtract( bidAmount );
         } else {
            sellProfit = sellProfit.add( sellAmount.multiply( bidPrice ) );
            sellAmount = BigDecimal.ZERO;
         }

         if ( sellAmount.equals( BigDecimal.ZERO ) ){
            return sellProfit;
         }
      }

      throw new RuntimeException("Not enough bids data to fill Sell order. Try increasing limit");
   }

   /**
    * Fill Buy order using asks data from order book.
    *
    * @param tradeAmount the amount of coin used to buy/trade from sell orders
    * @param asks      an array of sell limit orders from order book where each sell order is [askPrice, askAmount] so the array
    *                  will be like [ [askPrice1, askAmount1], [askPrice2, askAmount2], ... ]
    * @return
    */
   public BigDecimal fillBuyOrder(BigDecimal tradeAmount, String[][] asks) {
      BigDecimal askPrice = null;
      BigDecimal askAmount = null;
      BigDecimal buyAmount = BigDecimal.ZERO; // amount of coins bought/traded
      List<String[]> asksList = Arrays.asList( asks );

      for (String[] ask : asksList) {
         askPrice = new BigDecimal(ask[0]);
         askAmount = new BigDecimal(ask[1]);

         BigDecimal costAmount = askAmount.multiply( askPrice );
         if ( tradeAmount.compareTo( costAmount ) > 0 ) {
            buyAmount = buyAmount.add( askAmount );
            tradeAmount = tradeAmount.subtract( costAmount );
         } else {
            buyAmount = buyAmount.add( tradeAmount.divide( askPrice, 6, RoundingMode.HALF_UP ) );
            tradeAmount = BigDecimal.ZERO;
         }

         if ( tradeAmount.equals( BigDecimal.ZERO ) ){
            return buyAmount;
         }
      }
      throw new RuntimeException("Not enough asks data to fill Buy order. Try increasing limit");
   }

}
