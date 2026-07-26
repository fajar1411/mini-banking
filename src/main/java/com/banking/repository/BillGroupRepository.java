package com.banking.repository;

import com.banking.entity.BillGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillGroupRepository extends JpaRepository<BillGroup, Long> {

    List<BillGroup> findByOwnerId(Long ownerId);
}