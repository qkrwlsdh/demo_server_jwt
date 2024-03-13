package com.example.springjwt.service;

import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.entity.LoginHistoryEntity;
import com.example.springjwt.entity.UserEntity;
import com.example.springjwt.repository.LoginHistoryRepository;
import com.example.springjwt.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public CustomUserDetailsService(UserRepository userRepository, LoginHistoryRepository loginHistoryRepository) {

        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userData = userRepository.findByLoginId(username);

        if (userData != null) {

            return new CustomUserDetails(userData);
        }

        return null;
    }

    public String findByGoogleOtp(String loginId) {
        return userRepository.findByGoogleOtp(loginId);
    }

    @Transactional
    public void updateFailCnt(String loginId, long failCnt) {
        UserEntity userEntity = userRepository.findByLoginId(loginId);

        userEntity.setFailCnt(failCnt);
    }

    public void createLoginHistory(LoginHistoryEntity loginHistoryEntity) {
        loginHistoryRepository.save(loginHistoryEntity);
    }
}
