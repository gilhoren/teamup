package com.joytech.teamup.dao;

import com.joytech.teamup.dto.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {

}

