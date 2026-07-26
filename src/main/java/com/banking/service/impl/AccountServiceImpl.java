package com.banking.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import com.banking.dtos.request.DepositWithdrawRequest;
import com.banking.dtos.request.FilterRequest;
import com.banking.dtos.request.PaginationRequest;
import com.banking.dtos.request.SortingRequest;
import com.banking.dtos.request.TransferRequest;
import com.banking.dtos.response.AccountResponse;
import com.banking.dtos.response.AccountSummaryResponse;
import com.banking.dtos.response.ApiResponse;
import com.banking.dtos.response.PaginationResponse;
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

    private final JdbcTemplate jdbcTemplate;

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
    public ApiResponse<AccountResponse> getAccount(String accountNumber) {
        return mapApiResponse(findAccount(accountNumber));
    }

    @Override
    public List<AccountResponse> getUserAccounts(User user, LocalDate start, LocalDate end ) {
        return accountRepository.findByUser(user.getId())
                .stream()
                .filter(acc -> !acc.getCreatedAt().toLocalDate().isBefore(start) && !acc.getCreatedAt().toLocalDate().isAfter(end))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse deposit(String accountNumber, DepositWithdrawRequest request) {
        Account account = findAccount(accountNumber);


        if(request.getAmount().compareTo(BigDecimal.ZERO) <0){
             throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Saldo tidak Boleh minus"
            );
        }
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        accountRepository.updateBalance(accountNumber, newBalance);
        account.setBalance(newBalance);
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse transfer(String accountNumber, TransferRequest request) {
        Account sender = findAccount(accountNumber);

        Account receiver = findAccount(request.getReceiverAccount());

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Nomor acount tidak boleh sama"
            );
        }

        if (sender.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Saldo tidak mencukupi"
            );
        }
        BigDecimal senderBalance = sender.getBalance()
                .subtract(request.getAmount());

        if (senderBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Anda Transfer melampaui saldo"
            );
        }

        sender.setBalance(senderBalance);

        BigDecimal receiverBalance = receiver.getBalance()
                .add(request.getAmount());

        receiver.setBalance(receiverBalance);

        accountRepository.save(receiver);

        accountRepository.save(sender);

        return mapToResponse(sender);
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

    private ApiResponse<AccountResponse> mapApiResponse(Account account) {

        ApiResponse response = new ApiResponse<>();

        response.setData(AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build());

        return response;
    }

    @Override
    public AccountSummaryResponse getAccountSummary(User user) {
        List<Account> accounts = accountRepository.findByUser(user.getId());

        // disini saya memfilter account si user yang login untuk menghitung total akun
        // yang aktif
        long totalActive = accounts.stream()
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .count();

        // disini saya mengambil balance m untuk di gabungkan menjadi
        // satu dan lalu di jumlahkan
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // disini langkah pertama saya memfilter akun yang aktif
        // lalu di sorted itu saya menguruktkan dari yang terbesar ke terkecil
        // lalu saya mengambil no akunnya
        // untuk mengambil nomor akun yang terbesar balancenya dengan status active
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

    @Override
    public ApiResponse<PaginationResponse<AccountResponse>> findAllAccount(
            PaginationRequest request) {

        String sql = "SELECT * FROM accounts WHERE 1=1 ";
        String countSql = "SELECT COUNT(*) FROM accounts WHERE 1=1 ";

        List<Object> params = new ArrayList<>();

        // ========== FILTER ==========
        if (request.getFilterLists() != null) {
            for (FilterRequest f : request.getFilterLists()) {

                if ("EQUALS".equalsIgnoreCase(f.getCondition())) {
                    sql += " AND " + f.getFieldName() + " = ? ";
                    countSql += " AND " + f.getFieldName() + " = ? ";
                    params.add(f.getValue());
                }

                if ("LIKE".equalsIgnoreCase(f.getCondition())) {
                    sql += " AND " + f.getFieldName() + " LIKE ? ";
                    countSql += " AND " + f.getFieldName() + " LIKE ? ";
                    params.add("%" + f.getValue() + "%");
                }

                if ("GREATER_THAN".equalsIgnoreCase(f.getCondition())) {
                    sql += " AND " + f.getFieldName() + " > ? ";
                    countSql += " AND " + f.getFieldName() + " > ? ";
                    params.add(f.getValue());
                }

                if ("LESS_THAN".equalsIgnoreCase(f.getCondition())) {
                    sql += " AND " + f.getFieldName() + " < ? ";
                    countSql += " AND " + f.getFieldName() + " < ? ";
                    params.add(f.getValue());
                }
            }
        }

        // ========== SORT ==========
        if (request.getSortingRequests() != null
                && !request.getSortingRequests().isEmpty()) {

            SortingRequest sort = request.getSortingRequests().get(0);

            sql += " ORDER BY " + sort.getFieldName()
                    + " " + sort.getCondition();
        }

        // ========== PAGINATION ==========
        int page = request.getPage();
        int size = request.getSize();
        int offset = (page - 1) * size;

        sql += " LIMIT ? OFFSET ? ";

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add(offset);

        // ========== QUERY DATA ==========
        List<AccountResponse> data = jdbcTemplate.query(
                sql,
                queryParams.toArray(),
                (rs, rowNum) -> AccountResponse.builder()
                        .id(rs.getLong("id"))
                        .accountNumber(rs.getString("account_number"))
                        .balance(rs.getBigDecimal("balance"))
                        .status(AccountStatus.valueOf(rs.getString("status")))
                        .build()
        );

        // ========== COUNT ==========
        Long total = jdbcTemplate.queryForObject(
                countSql,
                params.toArray(),
                Long.class
        );

        int totalPages = (int) Math.ceil((double) total / size);

        // ========== RESPONSE ==========
        PaginationResponse<AccountResponse> pagination
                = PaginationResponse.<AccountResponse>builder()
                        .data(data)
                        .currentPage(page)
                        .pageSize(size)
                        .totalData(total)
                        .totalPages(totalPages)
                        .build();

        ApiResponse<PaginationResponse<AccountResponse>> response
                = new ApiResponse<>();

        response.setData(pagination);

        return response;
    }
}
