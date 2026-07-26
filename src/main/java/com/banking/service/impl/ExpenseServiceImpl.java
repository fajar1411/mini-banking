package com.banking.service.impl;

import com.banking.dtos.request.CreateExpenseRequest;
import com.banking.dtos.response.ExpenseResponse;
import com.banking.entity.BillGroup;
import com.banking.entity.Expense;
import com.banking.entity.ExpenseShare;
import com.banking.entity.Participant;
import com.banking.repository.BillGroupRepository;
import com.banking.repository.ExpenseRepository;
import com.banking.repository.ParticipantRepository;
import com.banking.service.ExpenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional
    public ExpenseResponse create(
            Long groupId,
            CreateExpenseRequest request
    ) {

        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bill group not found"
                        )
                );

        Participant payer = participantRepository
                .findById(request.getPaidByParticipantId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payer participant not found"
                        )
                );

        validateParticipantInGroup(
                payer,
                groupId,
                "Payer does not belong to this group"
        );

        Expense expense = Expense.builder()
                .group(group)
                .paidBy(payer)
                .amount(request.getAmount())
                .description(request.getDescription())
                .splitType(com.banking.entity.SplitType.EQUAL)
                .build();

        BigDecimal share = request.getAmount()
                .divide(
                        BigDecimal.valueOf(
                                request.getParticipantIds().size()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        for (Long participantId :
                request.getParticipantIds()) {

            Participant participant =
                    participantRepository.findById(participantId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Participant not found: "
                                                    + participantId
                                    )
                            );

            validateParticipantInGroup(
                    participant,
                    groupId,
                    "Participant does not belong to this group"
            );

            ExpenseShare expenseShare =
                    ExpenseShare.builder()
                            .expense(expense)
                            .participant(participant)
                            .amount(share)
                            .build();

            expense.getShares().add(expenseShare);
        }

        Expense savedExpense =
                expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    private void validateParticipantInGroup(
            Participant participant,
            Long groupId,
            String message
    ) {

        if (!participant.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException(message);
        }
    }

    private ExpenseResponse mapToResponse(
            Expense expense
    ) {

        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .paidByParticipantId(
                        expense.getPaidBy().getId()
                )
                .paidByParticipantName(
                        expense.getPaidBy().getName()
                )
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .splitType(expense.getSplitType())
                .build();
    }
}