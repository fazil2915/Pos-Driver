package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TerminalRepo extends JpaRepository<Terminal, UUID> {
    Optional<Terminal> findBySerialNumber(String serialNumber);
}
