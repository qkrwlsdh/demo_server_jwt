package com.example.springjwt.controller;

import com.example.springjwt.dto.LoginHistoryDTO;
import com.example.springjwt.entity.LoginHistoryEntity;
import com.example.springjwt.entity.UserEntity;
import com.example.springjwt.jwt.JWTUtil;
import com.example.springjwt.service.CustomUserDetailsService;
import com.example.springjwt.service.MailService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final CustomUserDetailsService customUserDetailsService;
    private final JWTUtil jwtUtil;
    private final MailService mailService;

    public LoginController(CustomUserDetailsService customUserDetailsService, JWTUtil jwtUtil, MailService mailService) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
    }

    @PostMapping("/auth")
    public ResponseEntity<?> otpVerify(@RequestBody Map<String, String> data, LoginHistoryDTO loginHistoryDTO, HttpServletResponse response) throws UnknownHostException {
        HttpStatus status;

        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        String otpKey = customUserDetailsService.findByGoogleOtp(data.get("loginId"));
        String reissueYn = customUserDetailsService.findByPwReissueYn(data.get("loginId"));
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

        return new ResponseEntity<>(reissueYn, status);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        // cookie.setSecure(true);
        cookie.setPath("/");
        // cookie.setHttpOnly(true);

        return cookie;
    }

    /**
     * 아이디 찾기
     * @param data 사업자번호, 대표자 생년월일
     */
    @PostMapping("/findUsername")
    public void findUsername(@RequestBody Map<String, String> data, HttpServletResponse response) throws Exception {
        HttpStatus status;
        try {
            UserEntity user = customUserDetailsService.findByUser(data.get("ccNo"), data.get("repBirthDt"));
            mailConfirm(user.getEmail(), user.getLoginId());

            status = HttpStatus.OK;
            response.setStatus(status.value());

        } catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            response.setStatus(status.value());
            log.error("Error while finding user", e);
        }
    }

    /**
     * 비밀번호 찾기
     * @param data 사업자번호, 대표자 생년월일, 아이디
     */
    @PostMapping("/findUserPassword")
    public void findUserPassword(@RequestBody Map<String, String> data, HttpServletResponse response) throws Exception {
        HttpStatus status;
        try {
            UserEntity user = customUserDetailsService.findByUserPw(data.get("ccNo"), data.get("repBirthDt"), data.get("username"));
            mailConfirmPw(user.getEmail(), user.getTemporaryPw());

            status = HttpStatus.OK;
            response.setStatus(status.value());

        } catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            response.setStatus(status.value());
            log.error("Error while finding user", e);
        }
    }

    /**
     * 비밀번호 재설정
     * @param data 아이디, 사업자 번호, 생년월일
     * @return ResponseEntity
     */
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> data) throws Exception {
        HttpStatus status;
        try {
            customUserDetailsService.changePassword(data);
            status = HttpStatus.OK;
        } catch (  Exception e ) {
            log.error("Error while changing password", e);
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(status);
    }

    /**
     * 아이디 찾기 메일 송신
     * @param email 수신받을 사용자 이메일
     * @param username 사용자 아이디
     */
    public void mailConfirm(@RequestParam String email, @RequestParam String username) throws Exception {
        mailService.sendSimpleMessage(email, username, "id");
    }

    /**
     * 비밀번호 찾기 메일 송신
     * @param email 수신받을 사용자 이메일
     * @param password 임시 비밀번호
     */
    public void mailConfirmPw(@RequestParam String email, @RequestParam String password) throws Exception {
        mailService.sendSimpleMessage(email, password, "pw");
    }
}
