package com.example.springjwt.repository;

import com.example.springjwt.entity.LoginHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LoginHistoryRepository extends JpaRepository<LoginHistoryEntity, LocalDateTime> {

}
