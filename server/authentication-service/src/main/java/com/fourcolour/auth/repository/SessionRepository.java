package com.fourcolour.auth.repository;

import com.fourcolour.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<Session> findByToken(String token);
    
    @Query("SELECT s FROM Session s WHERE s.token = :token AND s.expiresAt > :now")
    Optional<Session> findByTokenAndNotExpired(String token, LocalDateTime now);
    
    @Modifying
    @Transactional
    void deleteByToken(String token);
    
    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    void deleteExpiredSessions(LocalDateTime now);
} 