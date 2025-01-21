package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Switch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SwitchRepo extends JpaRepository<Switch, UUID> {
}
