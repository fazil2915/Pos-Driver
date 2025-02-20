package com.example.pos_driver.Repo;


import com.example.pos_driver.Model.LogConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogConfigRepo extends JpaRepository<LogConfig, Long> {
}
