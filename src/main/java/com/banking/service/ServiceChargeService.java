package com.banking.service;

import java.math.BigDecimal;

public interface ServiceChargeService {

    BigDecimal getPercentage();

    BigDecimal calculateAmount(BigDecimal totalExpenses);
}