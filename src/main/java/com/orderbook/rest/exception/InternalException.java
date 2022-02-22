package com.orderbook.rest.exception;

import com.orderbook.rest.dto.BaseResponse;
import lombok.Data;


@Data
public class InternalException extends Exception {

    private String outcomeCode;
    private String outcomeMessage;
    private String internalMessage;

    private String apiUri;

    public InternalException(String outcomeCode, String outcomeMessage, String internalMessage){
        this.outcomeCode = outcomeCode;
        this.outcomeMessage = outcomeMessage;
        this.internalMessage = internalMessage;
    }

    public InternalException(BaseResponse resp, String apiUri){
        this.outcomeCode = resp.getOutcomeCode();
        this.outcomeMessage = resp.getOutcomeMessage();
        this.internalMessage = resp.getOutcomeUserMessage();
        this.apiUri = apiUri;
    }
}

