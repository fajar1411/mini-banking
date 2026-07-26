package com.banking.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {

    private Long groupId;
    private String groupName;
    private BigDecimal totalExpenses;

    private BigDecimal serviceChargePct;
    private BigDecimal serviceChargeAmount;

    private List<SettlementItem> settlements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementItem {

        private Long fromParticipantId;
        private String fromParticipantName;

        private Long toParticipantId;
        private String toParticipantName;

        private BigDecimal amount;
    }
}