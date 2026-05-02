package com.banking.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.response.AccountResponse;
import com.banking.dtos.response.AccountSummaryResponse;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.enums.AccountStatus;
import com.banking.repository.AccountRepository;
import com.banking.service.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public AccountResponse createAccount(User user) {
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .build();

        return mapToResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse getAccount(String accountNumber) {
        return mapToResponse(findAccount(accountNumber));
    }

    @Override
    public List<AccountResponse> getUserAccounts(User user) {
        return accountRepository.findByUser(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse deposit(String accountNumber, DepositWithdrawRequest request) {
        Account account = findAccount(accountNumber);
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        accountRepository.updateBalance(accountNumber, newBalance);
        account.setBalance(newBalance);
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse withdraw(String accountNumber, DepositWithdrawRequest request) {
        Account account = findAccount(accountNumber);
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Saldo tidak cukup");
        }
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        accountRepository.updateBalance(accountNumber, newBalance);
        account.setBalance(newBalance);
        return mapToResponse(account);
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Akun tidak ditemukan: " + accountNumber));
    }

    private String generateAccountNumber() {
        return "ACC" + String.format("%010d", new Random().nextLong(9_999_999_999L));
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Override
    public AccountSummaryResponse getAccountSummary(User user) {
        List<Account> accounts = accountRepository.findByUser(user.getId());

        // disini saya memfilter account si user yang login untuk menghitung total akun
        // yang aktif
        long totalActive = accounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .count();

        // disini saya mengambil balance menjadi satu untuk di gabungkan menjadi
        // satu dan lalu di jumlahkan
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // disini langkah pertama saya memfilter akun yang aktif
        // lalu di sorted itu saya menguruktkan dari yang terkecil ke terbesar
        // lalu saya mengambil no akunnya
        // untuk diambil data(nilai) pertama yang terbesar dari akun tersebut
        String richestAccountNumber = accounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .sorted((a, b) -> b.getBalance().compareTo(a.getBalance()))
                .map(Account::getAccountNumber)
                .findFirst()
                .orElse("Tidak ada akun aktif");

        return AccountSummaryResponse.builder()
                .totalAccounts(accounts.size())
                .totalActiveAccounts((int) totalActive)
                .totalBalance(totalBalance)
                .richestAccountNumber(richestAccountNumber)
                .build();
    }
}