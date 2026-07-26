package com.banking.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillGroupResponse {

    private Long id;
    private String name;

    private Long ownerId;
    private String ownerUsername;

    private LocalDateTime createdAt;

    private List<ParticipantResponse> participants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantResponse {

        private Long id;
        private Long userId;
        private String username;
    }
}