package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantRepo extends JpaRepository<Merchant, UUID> {
}
