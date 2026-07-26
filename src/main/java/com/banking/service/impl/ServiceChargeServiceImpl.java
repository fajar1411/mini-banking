package com.banking.service.impl;

import com.banking.service.ServiceChargeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ServiceChargeServiceImpl implements ServiceChargeService {

    private final String githubUsername;

    public ServiceChargeServiceImpl(
            @Value("${app.github-username}") String githubUsername
    ) {
        this.githubUsername = githubUsername;
    }

    @Override
    public BigDecimal getPercentage() {

        int sum = githubUsername
                .toLowerCase()
                .chars()
                .sum();

        return BigDecimal.valueOf(sum % 10);
    }

    @Override
    public BigDecimal calculateAmount(
            BigDecimal totalExpenses
    ) {

        return totalExpenses
                .multiply(getPercentage())
                .divide(BigDecimal.valueOf(100));
    }
}