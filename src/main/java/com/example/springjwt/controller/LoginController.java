package com.example.springjwt.controller;

import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.dto.LoginHistoryDTO;
import com.example.springjwt.entity.LoginHistoryEntity;
import com.example.springjwt.jwt.JWTUtil;
import com.example.springjwt.service.CustomUserDetailsService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final CustomUserDetailsService customUserDetailsService;
    private final JWTUtil jwtUtil;

    public LoginController(CustomUserDetailsService customUserDetailsService, JWTUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> otpVerify(@RequestBody Map<String, String> data, LoginHistoryDTO loginHistoryDTO, HttpServletResponse response) throws UnknownHostException {
        HttpStatus status;

        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        String otpKey = customUserDetailsService.findByGoogleOtp(data.get("loginId"));
        boolean verify = googleAuthenticator.authorize(otpKey, Integer.parseInt(data.get("token")));

        logger.info("VerifyCode : {}", Integer.parseInt(data.get("token")));
        logger.info("Verify : {}", verify);

        if (!verify) {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }

        LoginHistoryEntity loginHistoryEntity = new LoginHistoryEntity(
                data.get("loginId"),
                loginHistoryDTO.getLoginDt(),
                "Y"
        );
        customUserDetailsService.createLoginHistory(loginHistoryEntity);

        // 로그인 성공 시 실패 카운트 0
        customUserDetailsService.updateFailCnt(data.get("loginId"), 0);

        // 토큰 생성
        String access = jwtUtil.createJwt("access", data.get("loginId"), "ROLE_ADMIN", 600000L);
        String refresh = jwtUtil.createJwt("refresh", data.get("loginId"), "ROLE_ADMIN", 86400000L);

        // 응답 설정
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        status = HttpStatus.OK;

        return new ResponseEntity<>(status);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        // cookie.setSecure(true);
        cookie.setPath("/");
        // cookie.setHttpOnly(true);

        return cookie;
    }
}
