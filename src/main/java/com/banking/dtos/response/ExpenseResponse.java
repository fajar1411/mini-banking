package com.banking.dtos.response;

import com.banking.entity.SplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;

    private Long groupId;

    private Long paidByParticipantId;

    private String paidByParticipantName;

    private BigDecimal amount;

    private String description;

    private SplitType splitType;
}