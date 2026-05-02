package com.banking.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.banking.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "SELECT * FROM accounts WHERE account_number = :accountNumber", nativeQuery = true)
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query(value = "SELECT * FROM accounts WHERE user_id = :userId", nativeQuery = true)
    List<Account> findByUser(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE accounts SET balance = :balance WHERE account_number = :accountNumber", nativeQuery = true)
    void updateBalance(@Param("accountNumber") String accountNumber, @Param("balance") BigDecimal balance);
}