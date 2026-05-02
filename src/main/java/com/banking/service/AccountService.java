package com.banking.service;

import java.util.List;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.response.AccountResponse;
import com.banking.dtos.response.AccountSummaryResponse;
import com.banking.entity.User;

public interface AccountService {
    AccountResponse createAccount(User user);
     AccountSummaryResponse getAccountSummary(User user);
    AccountResponse getAccount(String accountNumber);
    List<AccountResponse> getUserAccounts(User user);
    AccountResponse deposit(String accountNumber, DepositWithdrawRequest request);
    AccountResponse withdraw(String accountNumber, DepositWithdrawRequest request);
}