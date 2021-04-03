package com.joytech.teamup.dao;

import com.joytech.teamup.dto.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    @Query("select t from VerificationToken t where t.token = :token")
    public List<VerificationToken> find(@Param("token") String token);
}
