package com.banking.service;

import java.util.List;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.request.PaginationRequest;
import com.banking.dtos.request.TransferRequest;
import com.banking.dtos.response.AccountResponse;
import com.banking.dtos.response.AccountSummaryResponse;
import com.banking.dtos.response.ApiResponse;
import com.banking.dtos.response.PaginationResponse;
import com.banking.entity.User;
import java.time.LocalDate;

public interface AccountService {
    AccountResponse createAccount(User user);
    ApiResponse<PaginationResponse<AccountResponse>>findAllAccount(PaginationRequest request);
    AccountSummaryResponse getAccountSummary(User user);
    ApiResponse<AccountResponse> getAccount(String accountNumber);
    List<AccountResponse> getUserAccounts(User user, LocalDate start, LocalDate end);
    AccountResponse deposit(String accountNumber, DepositWithdrawRequest request);
    AccountResponse withdraw(String accountNumber, DepositWithdrawRequest request);
     AccountResponse transfer(String accountNumber, TransferRequest request);
}