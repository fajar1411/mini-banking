package com.banking.service.impl;

import com.banking.dtos.request.CreateSettlementRequest;
import com.banking.dtos.response.SettlementResponse;
import com.banking.dtos.response.TransactionResponse;
import com.banking.entity.Account;
import com.banking.entity.BillGroup;
import com.banking.entity.Expense;
import com.banking.entity.ExpenseShare;
import com.banking.entity.Participant;
import com.banking.entity.Transaction;
import com.banking.enums.TransactionType;
import com.banking.repository.AccountRepository;
import com.banking.repository.BillGroupRepository;
import com.banking.repository.ParticipantRepository;
import com.banking.repository.TransactionRepository;
import com.banking.service.ServiceChargeService;
import com.banking.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ServiceChargeService serviceChargeService;

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse calculate(Long groupId) {

        BillGroup group =
                billGroupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bill group not found: " + groupId
                                )
                        );

        Map<Long, Participant> participants =
                new HashMap<>();

        for (Participant participant :
                group.getParticipants()) {

            if (participant == null) {
                continue;
            }

            if (participant.getId() == null) {
                throw new IllegalArgumentException(
                        "Participant ID cannot be null"
                );
            }

            participants.put(
                    participant.getId(),
                    participant
            );
        }

        Map<Long, BigDecimal> balances =
                new LinkedHashMap<>();

        for (Participant participant :
                group.getParticipants()) {

            balances.put(
                    participant.getId(),
                    BigDecimal.ZERO
            );
        }

        BigDecimal totalExpenses =
                BigDecimal.ZERO;

        if (group.getExpenses() != null) {

            for (Expense expense :
                    group.getExpenses()) {

                if (expense == null) {
                    continue;
                }

                if (expense.getPaidBy() == null) {
                    throw new IllegalArgumentException(
                            "Expense payer cannot be null"
                    );
                }

                Long payerParticipantId =
                        expense.getPaidBy().getId();

                if (payerParticipantId == null) {
                    throw new IllegalArgumentException(
                            "Payer participant ID cannot be null"
                    );
                }

                if (!participants.containsKey(
                        payerParticipantId
                )) {
                    throw new IllegalArgumentException(
                            "Payer participant does not belong "
                                    + "to bill group"
                    );
                }

                totalExpenses =
                        totalExpenses.add(
                                expense.getAmount()
                        );

                if (expense.getShares() != null) {

                    for (ExpenseShare share :
                            expense.getShares()) {

                        if (share == null) {
                            continue;
                        }

                        if (share.getParticipant() == null) {
                            throw new IllegalArgumentException(
                                    "Expense share participant "
                                            + "cannot be null"
                            );
                        }

                        Long participantId =
                                share.getParticipant().getId();

                        if (participantId == null) {
                            throw new IllegalArgumentException(
                                    "Expense share participant ID "
                                            + "cannot be null"
                            );
                        }

                        if (!participants.containsKey(
                                participantId
                        )) {
                            throw new IllegalArgumentException(
                                    "Expense share participant does "
                                            + "not belong to bill group"
                            );
                        }

                        balances.computeIfPresent(
                                participantId,
                                (id, balance) ->
                                        balance.subtract(
                                                share.getAmount()
                                        )
                        );
                    }
                }

                balances.computeIfPresent(
                        payerParticipantId,
                        (id, balance) ->
                                balance.add(
                                        expense.getAmount()
                                )
                );
            }
        }

        List<BalanceEntry> creditors =
                balances.entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getValue()
                                        .compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                        )
                        .map(entry ->
                                new BalanceEntry(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        BalanceEntry::amount
                                ).reversed()
                        )
                        .toList();

        List<BalanceEntry> debtors =
                balances.entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getValue()
                                        .compareTo(
                                                BigDecimal.ZERO
                                        ) < 0
                        )
                        .map(entry ->
                                new BalanceEntry(
                                        entry.getKey(),
                                        entry.getValue()
                                                .abs()
                                )
                        )
                        .sorted(
                                Comparator.comparing(
                                        BalanceEntry::amount
                                ).reversed()
                        )
                        .toList();

        List<SettlementResponse.SettlementItem>
                settlements =
                new ArrayList<>();

        int creditorIndex = 0;
        int debtorIndex = 0;

        while (
                creditorIndex < creditors.size()
                        && debtorIndex < debtors.size()
        ) {

            BalanceEntry creditor =
                    creditors.get(
                            creditorIndex
                    );

            BalanceEntry debtor =
                    debtors.get(
                            debtorIndex
                    );

            Participant from =
                    participants.get(
                            debtor.participantId()
                    );

            Participant to =
                    participants.get(
                            creditor.participantId()
                    );

            if (from == null) {
                throw new IllegalArgumentException(
                        "From participant not found"
                );
            }

            if (to == null) {
                throw new IllegalArgumentException(
                        "To participant not found"
                );
            }

            if (from.getId().equals(
                    to.getId()
            )) {
                throw new IllegalArgumentException(
                        "From participant and To participant "
                                + "cannot be the same"
                );
            }

            BigDecimal amount =
                    creditor.amount()
                            .min(
                                    debtor.amount()
                            );

            if (amount.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {
                throw new IllegalArgumentException(
                        "Settlement amount must be greater "
                                + "than zero"
                );
            }

            settlements.add(
                    new SettlementResponse.SettlementItem(
                            from.getId(),
                            from.getName(),
                            to.getId(),
                            to.getName(),
                            amount
                    )
            );

            BigDecimal creditorRemaining =
                    creditor.amount()
                            .subtract(
                                    amount
                            );

            BigDecimal debtorRemaining =
                    debtor.amount()
                            .subtract(
                                    amount
                            );

            creditors =
                    replace(
                            creditors,
                            creditorIndex,
                            new BalanceEntry(
                                    creditor.participantId(),
                                    creditorRemaining
                            )
                    );

            debtors =
                    replace(
                            debtors,
                            debtorIndex,
                            new BalanceEntry(
                                    debtor.participantId(),
                                    debtorRemaining
                            )
                    );

            if (creditorRemaining.signum() == 0) {
                creditorIndex++;
            }

            if (debtorRemaining.signum() == 0) {
                debtorIndex++;
            }
        }

        BigDecimal serviceChargePct =
                serviceChargeService
                        .getPercentage();

        BigDecimal serviceChargeAmount =
                serviceChargeService
                        .calculateAmount(
                                totalExpenses
                        );

        return SettlementResponse.builder()
                .groupId(
                        group.getId()
                )
                .groupName(
                        group.getName()
                )
                .totalExpenses(
                        totalExpenses
                )
                .serviceChargePct(
                        serviceChargePct
                )
                .serviceChargeAmount(
                        serviceChargeAmount
                )
                .settlements(
                        settlements
                )
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse pay(
            Long groupId,
            CreateSettlementRequest request
    ) {

        BillGroup group =
                billGroupRepository.findById(
                        groupId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bill group not found: "
                                        + groupId
                        )
                );

        Participant fromParticipant =
                participantRepository.findById(
                        request.getFromParticipantId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "From participant not found: "
                                        + request.getFromParticipantId()
                        )
                );

        Participant toParticipant =
                participantRepository.findById(
                        request.getToParticipantId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "To participant not found: "
                                        + request.getToParticipantId()
                        )
                );

        if (!fromParticipant
                .getGroup()
                .getId()
                .equals(group.getId())) {

            throw new IllegalArgumentException(
                    "From participant does not belong "
                            + "to this bill group"
            );
        }

        if (!toParticipant
                .getGroup()
                .getId()
                .equals(group.getId())) {

            throw new IllegalArgumentException(
                    "To participant does not belong "
                            + "to this bill group"
            );
        }

        if (fromParticipant
                .getId()
                .equals(
                        toParticipant.getId()
                )) {

            throw new IllegalArgumentException(
                    "From participant and To participant "
                            + "cannot be the same"
            );
        }

        if (request.getAmount()
                .compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new IllegalArgumentException(
                    "Settlement amount must be greater "
                            + "than zero"
            );
        }

        Long fromUserId =
                fromParticipant
                        .getUser()
                        .getId();

        Long toUserId =
                toParticipant
                        .getUser()
                        .getId();

        Account sourceAccount =
                accountRepository
                        .findByAccountNumber(
                                request.getFromAccountId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Source account not found: "
                                                + request.getFromAccountId()
                                )
                        );

        Account destinationAccount =
                accountRepository
                        .findByAccountNumber(
                                request.getToAccountId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destination account not found: "
                                                + request.getToAccountId()
                                )
                        );

        if (!sourceAccount
                .getUser()
                .getId()
                .equals(fromUserId)) {

            throw new IllegalArgumentException(
                    "Source account does not belong "
                            + "to from participant"
            );
        }

        if (!destinationAccount
                .getUser()
                .getId()
                .equals(toUserId)) {

            throw new IllegalArgumentException(
                    "Destination account does not belong "
                            + "to to participant"
            );
        }

        if (sourceAccount
                .getAccountNumber()
                .equals(
                        destinationAccount.getAccountNumber()
                )) {

            throw new IllegalArgumentException(
                    "Source account and destination account "
                            + "cannot be the same"
            );
        }

        if (sourceAccount
                .getBalance()
                .compareTo(
                        request.getAmount()
                ) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient balance"
            );
        }

        sourceAccount.setBalance(
                sourceAccount
                        .getBalance()
                        .subtract(
                                request.getAmount()
                        )
        );

        destinationAccount.setBalance(
                destinationAccount
                        .getBalance()
                        .add(
                                request.getAmount()
                        )
        );

        Transaction transaction =
                Transaction.builder()
                        .type(
                                TransactionType.TRANSFER
                        )
                        .amount(
                                request.getAmount()
                        )
                        .description(
                                request.getDescription()
                        )
                        .sourceAccount(
                                sourceAccount
                        )
                        .destinationAccount(
                                destinationAccount
                        )
                        .build();

        Transaction savedTransaction =
                transactionRepository.save(
                        transaction
                );

        return TransactionResponse.builder()
                .id(
                        savedTransaction.getId()
                )
                .type(
                        savedTransaction.getType()
                )
                .amount(
                        savedTransaction.getAmount()
                )
                .description(
                        savedTransaction.getDescription()
                )
                .sourceAccountNumber(
                        savedTransaction
                                .getSourceAccount()
                                .getAccountNumber()
                )
                .destinationAccountNumber(
                        savedTransaction
                                .getDestinationAccount()
                                .getAccountNumber()
                )
                .createdAt(
                        savedTransaction.getCreatedAt()
                )
                .build();
    }

    private List<BalanceEntry> replace(
            List<BalanceEntry> list,
            int index,
            BalanceEntry value
    ) {

        List<BalanceEntry> result =
                new ArrayList<>(
                        list
                );

        result.set(
                index,
                value
        );

        return result;
    }

    private record BalanceEntry(
            Long participantId,
            BigDecimal amount
    ) {
    }
}