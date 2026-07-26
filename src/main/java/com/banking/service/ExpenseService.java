package com.banking.service;

import com.banking.dtos.request.CreateExpenseRequest;
import com.banking.dtos.response.ExpenseResponse;

public interface ExpenseService {

    ExpenseResponse create(
            Long groupId,
            CreateExpenseRequest request
    );
}