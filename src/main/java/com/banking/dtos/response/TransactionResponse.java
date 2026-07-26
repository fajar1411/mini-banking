package com.banking.dtos.response;

import com.banking.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private LocalDateTime createdAt;
}