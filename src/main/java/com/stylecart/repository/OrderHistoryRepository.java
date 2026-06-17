package com.stylecart.repository;

import com.stylecart.model.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository
        extends JpaRepository<OrderHistory, Long> {

}