package com.banking.dtos.request;

import java.util.List;
import lombok.Data;
@Data
public class PaginationRequest {
     private Integer page = 1;

    private Integer size = 10;
    List<FilterRequest> filterLists;
    List<SortingRequest> sortingRequests;
}


