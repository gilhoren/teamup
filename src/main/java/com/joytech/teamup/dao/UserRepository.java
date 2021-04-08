package com.joytech.teamup.dao;

import com.joytech.teamup.dto.User;
import com.joytech.teamup.dto.VerificationToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer> {
    @Query("select u from User u where u.email = :email")
    public List<User> findUserByEmail(@Param("email") String email);
}

