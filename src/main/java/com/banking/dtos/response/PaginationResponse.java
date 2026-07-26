package com.banking.dtos.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationResponse<T> {

    private List<T> data;

    private Integer currentPage;

    private Integer pageSize;

    private Long totalData;

    private Integer totalPages;

}
