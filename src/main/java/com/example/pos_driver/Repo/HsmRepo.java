package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Hsm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HsmRepo extends JpaRepository<Hsm, UUID> {
}
