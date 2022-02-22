package com.orderbook.rest.dto;

import lombok.Data;

@Data
public class OrderBook {

   private String lastUpdateId;
   private String[][] bids;
   private String[][] asks;
}
