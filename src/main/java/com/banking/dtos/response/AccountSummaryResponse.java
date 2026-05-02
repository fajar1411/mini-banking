package com.banking.dtos.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountSummaryResponse {
    private int totalAccounts;
    private int totalActiveAccounts;
    private BigDecimal totalBalance;
    private String richestAccountNumber;
}