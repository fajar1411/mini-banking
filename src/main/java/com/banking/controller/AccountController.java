package com.banking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.request.PaginationRequest;
import com.banking.dtos.request.TransferRequest;
import com.banking.dtos.response.AccountResponse;
import com.banking.dtos.response.AccountSummaryResponse;
import com.banking.dtos.response.ApiResponse;
import com.banking.dtos.response.PaginationResponse;
import com.banking.entity.User;
import com.banking.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal User user) {
        AccountResponse response = accountService.createAccount(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountNumber}")
    public ApiResponse<AccountResponse> getAccount(
            @PathVariable String accountNumber) {
        ApiResponse<AccountResponse> AccountResponse;

        ApiResponse<AccountResponse> response = accountService.getAccount(accountNumber);
        return response;

    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            @AuthenticationPrincipal User user, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate ) {
        List<AccountResponse> responses = accountService.getUserAccounts(user, startDate, endDate);
        return ResponseEntity.ok(responses);
    }


      @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<AccountResponse> transfer(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransferRequest request) {
        AccountResponse response = accountService.transfer(accountNumber, request);
        return ResponseEntity.ok(response);
    }


       @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<AccountResponse>>> findAllAccount(
            @RequestBody PaginationRequest request) {

        ApiResponse<PaginationResponse<AccountResponse>> response =
                accountService.findAllAccount(request);

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositWithdrawRequest request) {
        AccountResponse response = accountService.deposit(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositWithdrawRequest request) {
        AccountResponse response = accountService.withdraw(accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.getAccountSummary(user));
    }
}
