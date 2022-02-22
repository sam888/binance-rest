package com.orderbook.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class BaseResponse {

    private String outcomeCode;
    private String outcomeMessage;
    private String outcomeUserMessage;

}
