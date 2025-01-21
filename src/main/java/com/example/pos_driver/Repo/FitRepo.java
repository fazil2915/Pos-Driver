package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.FinancialInstitution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FitRepo extends JpaRepository<FinancialInstitution, UUID> {
}
