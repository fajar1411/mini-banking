package com.banking.service;

import com.banking.dtos.request.CreateSettlementRequest;
import com.banking.dtos.response.SettlementResponse;
import com.banking.dtos.response.TransactionResponse;

public interface SettlementService {

    SettlementResponse calculate(Long groupId);

    TransactionResponse pay(
            Long groupId,
            CreateSettlementRequest request
    );
}