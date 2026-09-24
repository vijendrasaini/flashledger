package com.flashledger.flashledgerengine.repository;

import com.flashledger.flashledgerengine.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
}
