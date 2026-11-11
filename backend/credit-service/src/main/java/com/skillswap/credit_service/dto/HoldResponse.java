package com.skillswap.credit_service.dto;

import com.skillswap.credit_service.entity.CreditHold;
import com.skillswap.credit_service.entity.HoldStatus;

public record HoldResponse(Long exchangeId, Long payerId, Long payeeId, int amount, HoldStatus status) {

    public static HoldResponse from(CreditHold h) {
        return new HoldResponse(h.getExchangeId(), h.getPayerId(), h.getPayeeId(), h.getAmount(), h.getStatus());
    }
}
