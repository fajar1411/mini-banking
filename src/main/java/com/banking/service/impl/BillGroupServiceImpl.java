package com.banking.service.impl;

import com.banking.dtos.request.CreateBillGroupRequest;
import com.banking.dtos.response.BillGroupResponse;
import com.banking.entity.BillGroup;
import com.banking.entity.Participant;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.BillGroupRepository;
import com.banking.repository.UserRepository;
import com.banking.service.BillGroupService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillGroupServiceImpl implements BillGroupService {

    private final BillGroupRepository billGroupRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public BillGroupResponse create(CreateBillGroupRequest request) {

        // Participant pertama dianggap sebagai owner
        Long ownerUserId =
                request.getParticipantUserIds().get(0);

        // Cari owner
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Owner user not found: " + ownerUserId
                        )
                );

        // Owner wajib memiliki minimal 1 account
        validateUserHasAccount(ownerUserId);

        // Buat Bill Group
        BillGroup group = BillGroup.builder()
                .name(request.getName())
                .owner(owner)
                .build();

        // Buat participant
        for (Long userId : request.getParticipantUserIds()) {

            // Cari user
            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found: " + userId
                            )
                    );

            // User wajib memiliki minimal 1 account
            validateUserHasAccount(userId);

            Participant participant = Participant.builder()
                    .user(user)
                    .group(group)
                    .build();

            group.getParticipants().add(participant);
        }

        // Simpan group
        BillGroup savedGroup =
                billGroupRepository.save(group);

        return mapToResponse(savedGroup);
    }

   private void validateUserHasAccount(Long userId) {

    boolean hasAccount =
            !accountRepository.findByUser(userId).isEmpty();

    if (!hasAccount) {
        throw new RuntimeException(
                "User with ID " + userId
                        + " must have an account "
                        + "before joining a bill group"
        );
    }
}

    private BillGroupResponse mapToResponse(
            BillGroup group
    ) {

        List<BillGroupResponse.ParticipantResponse> participants =
                group.getParticipants()
                        .stream()
                        .map(participant ->
                                BillGroupResponse.ParticipantResponse
                                        .builder()
                                        .id(participant.getId())
                                        .userId(
                                                participant
                                                        .getUser()
                                                        .getId()
                                        )
                                        .username(
                                                participant
                                                        .getUser()
                                                        .getUsername()
                                        )
                                        .build()
                        )
                        .toList();

        return BillGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .ownerId(
                        group.getOwner().getId()
                )
                .ownerUsername(
                        group.getOwner().getUsername()
                )
                .createdAt(
                        group.getCreatedAt()
                )
                .participants(participants)
                .build();
    }
}