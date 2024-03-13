package com.example.springjwt.repository;

import com.example.springjwt.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Boolean existsByLoginId(String username);

    UserEntity findByLoginId(String username);

    @Query(value =  " SELECT" +
            " GOOGLE_OTP" +
            " FROM TUSER" +
            " WHERE LOGIN_ID = :loginId", nativeQuery = true)
    String findByGoogleOtp(@Param(value = "loginId") String loginId);
}
