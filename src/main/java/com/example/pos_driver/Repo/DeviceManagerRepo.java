package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.DeviceManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceManagerRepo extends JpaRepository<DeviceManager, UUID> {
}
