package com.example.testbit.repository;

import com.example.testbit.model.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    WalletEntity findByUserId(String userId);
}

