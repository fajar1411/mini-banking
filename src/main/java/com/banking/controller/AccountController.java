package com.banking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.response.AccountResponse;
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
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            @AuthenticationPrincipal User user) {
        List<AccountResponse> responses = accountService.getUserAccounts(user);
        return ResponseEntity.ok(responses);
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
}