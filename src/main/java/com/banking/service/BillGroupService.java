package com.banking.service;

import com.banking.dtos.request.CreateBillGroupRequest;
import com.banking.dtos.response.BillGroupResponse;

public interface BillGroupService {

    BillGroupResponse create(CreateBillGroupRequest request);
}