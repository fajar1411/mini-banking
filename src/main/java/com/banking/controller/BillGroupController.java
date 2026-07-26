package com.banking.controller;

import com.banking.dtos.request.CreateBillGroupRequest;
import com.banking.dtos.request.CreateExpenseRequest;
import com.banking.dtos.request.CreateSettlementRequest;
import com.banking.dtos.response.BillGroupResponse;
import com.banking.dtos.response.ExpenseResponse;
import com.banking.dtos.response.SettlementResponse;
import com.banking.dtos.response.TransactionResponse;
import com.banking.service.BillGroupService;
import com.banking.service.ExpenseService;
import com.banking.service.SettlementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill-groups")
@RequiredArgsConstructor
public class BillGroupController {

    private final BillGroupService billGroupService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<BillGroupResponse> createGroup(
            @Valid @RequestBody CreateBillGroupRequest request) {

        BillGroupResponse response =
                billGroupService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {

        ExpenseResponse response =
                expenseService.create(groupId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{groupId}/settlement")
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable Long groupId) {

        SettlementResponse response =
                settlementService.calculate(groupId);

        return ResponseEntity.ok(response);
    }

       @PostMapping("/{groupId}/settlement/pay")
    public ResponseEntity<TransactionResponse> paySettlement(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateSettlementRequest request) {

        TransactionResponse response =
                settlementService.pay(
                        groupId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}