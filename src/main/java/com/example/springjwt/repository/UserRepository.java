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

    @Query(value =  " SELECT" +
            " FAIL_CNT" +
            " FROM TUSER" +
            " WHERE LOGIN_ID = :loginId", nativeQuery = true)
    long findByFailCnt(@Param(value = "loginId") String username);

    @Query(value = " SELECT" +
            " *" +
            " FROM TUSER" +
            " WHERE CC_NO = :ccNo" +
            " AND REP_BIRTH_DT = :repBirthDt", nativeQuery = true)
    UserEntity findByUser(@Param(value = "ccNo") String ccNo,@Param(value = "repBirthDt") String repBirthDt);

    @Query(value = " SELECT" +
            " *" +
            " FROM TUSER" +
            " WHERE CC_NO = :ccNo" +
            " AND REP_BIRTH_DT = :repBirthDt" +
            " AND LOGIN_ID = :username", nativeQuery = true)
    UserEntity findByUserPw(@Param(value = "ccNo") String ccNo,@Param(value = "repBirthDt") String repBirthDt, @Param(value = "username") String username);

    @Query(value = " SELECT" +
            " PW_REISSUE_YN" +
            " FROM TUSER" +
            " WHERE LOGIN_ID = :loginId", nativeQuery = true)
    String findByPwReissueYn(@Param(value = "loginId") String loginId);
}
