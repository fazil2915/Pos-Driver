package com.example.pos_driver.Repo;

import com.example.pos_driver.Model.Key;
import org.hibernate.metamodel.model.convert.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KeyRepo extends JpaRepository<Key, UUID> {
}
