package com.banking.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {

    @NotNull
    private Long fromParticipantId;

    @NotNull
    private String fromAccountId;

    @NotNull
    private Long toParticipantId;

    @NotNull
    private String toAccountId;

    private String description;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}