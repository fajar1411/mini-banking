package com.banking.dtos.request;

import lombok.Data;

@Data
public class FilterRequest {
        String fieldName;
        String condition;
        String value;
}
