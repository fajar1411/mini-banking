package com.banking.service;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.response.AccountResponse;
import com.banking.entity.User;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(User user);
    AccountResponse getAccount(String accountNumber);
    List<AccountResponse> getUserAccounts(User user);
    AccountResponse deposit(String accountNumber, DepositWithdrawRequest request);
    AccountResponse withdraw(String accountNumber, DepositWithdrawRequest request);
}