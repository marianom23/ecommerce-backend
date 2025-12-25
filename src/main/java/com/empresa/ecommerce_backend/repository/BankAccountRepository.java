package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByActiveTrue();
}
