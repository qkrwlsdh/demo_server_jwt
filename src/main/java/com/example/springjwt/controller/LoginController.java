package com.example.springjwt.controller;

import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.dto.LoginHistoryDTO;
import com.example.springjwt.entity.LoginHistoryEntity;
import com.example.springjwt.service.CustomUserDetailsService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final CustomUserDetailsService customUserDetailsService;

    public LoginController(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> otpVerify(@RequestBody Map<String, String> data, CustomUserDetails userDTO, LoginHistoryDTO loginHistoryDTO) throws UnknownHostException {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> body = new HashMap<>();
        HttpStatus status;

        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        String otpKey = customUserDetailsService.findByGoogleOtp(data.get("loginId"));
        boolean verify = googleAuthenticator.authorize(otpKey, Integer.parseInt(data.get("token")));

        logger.info("VerifyCode : {}", Integer.parseInt(data.get("token")));
        logger.info("Verify : {}", verify);

        LoginHistoryEntity loginHistoryEntity = new LoginHistoryEntity(
                data.get("loginId"),
                loginHistoryDTO.getLoginDt(),
                "Y"
        );
        customUserDetailsService.createLoginHistory(loginHistoryEntity);

        // 로그인 성공 시 실패 카운트 0
        customUserDetailsService.updateFailCnt(data.get("loginId"), 0);

        body.put("verify", String.valueOf(verify));
        status = HttpStatus.OK;

        return new ResponseEntity<>(body, headers, status);
    }
}
