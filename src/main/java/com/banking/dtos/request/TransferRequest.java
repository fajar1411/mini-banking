package com.banking.dtos.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @Positive
    private BigDecimal amount;

    private String description;

    private String receiverAccount;
}