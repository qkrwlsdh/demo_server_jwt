package com.example.springjwt.service;

import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.entity.LoginHistoryEntity;
import com.example.springjwt.entity.UserEntity;
import com.example.springjwt.repository.LoginHistoryRepository;
import com.example.springjwt.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;

@Slf4j
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

    @Transactional
    public void createLoginHistory(LoginHistoryEntity loginHistoryEntity) {
        loginHistoryRepository.save(loginHistoryEntity);
    }

    public long findByFailCnt(String username) {
        return userRepository.findByFailCnt(username);
    }

    @Transactional
    public void updateLockYn(String username, String lockYn) {
        UserEntity userEntity = userRepository.findByLoginId(username);
        userEntity.setLockYn(lockYn);
    }

    // 사업자번호, 대표자 생년월일로 아이디 찾기
    public UserEntity findByUser(String ccNo, String repBirthDt) {

        return userRepository.findByUser(ccNo, repBirthDt);
    }

    // 사업자번호, 대표자 생년월일, 아이디로 비밀번호 찾기 (임시비밀번호 생성 후 업데이트)
    @Transactional
    public UserEntity findByUserPw(String ccNo, String repBirthDt, String username) {
        UserEntity user = userRepository.findByUserPw(ccNo, repBirthDt, username);

        String temporaryPw = generateRandomString(10);
        user.setTemporaryPw(temporaryPw);

        user.setLoginPw(encodeBcrypt(temporaryPw, 10));
        user.setPwReissueYn("Y");

        return user;
    }

    // 비밀번호 확인 후 재설정
    @Transactional
    public void changePassword(Map<String, String> data) throws IllegalArgumentException {
        UserEntity user = userRepository.findByLoginId(data.get("username"));
        boolean isMatches = encodeBcryptCheck(data.get("currentPw"), user.getLoginPw());
        if (!isMatches) {
            log.error("Current password is incorrect.");
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setLoginPw(encodeBcrypt(data.get("newPw"), 10));
        user.setPwReissueYn("N");
    }

    // 비밀번호 재설정 시 암호화
    public String encodeBcrypt(String planeText, int strength) {
        return new BCryptPasswordEncoder(strength).encode(planeText);
    }

    // 비밀번호 재설정 시 기존 비밀번호와 입력받은 비밀번호 검증
    public Boolean encodeBcryptCheck(String input, String encoded) {
        return new BCryptPasswordEncoder().matches(input, encoded);
    }

    // 해당 유저 DB에서 PW_REISSUE_YN을 조회
    public String findByPwReissueYn(String loginId) {
        return userRepository.findByPwReissueYn(loginId);
    }

    /**
     * 랜덤 문자열 생성
     * @param length 생성 길이
     * @return String
     */
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*";
    public String generateRandomString(int length) {
        // 랜덤한 문자열을 저장할 StringBuilder 객체 생성
        StringBuilder randomStringBuilder = new StringBuilder(length);
        // 보안성을 높이기 위해 SecureRandom 사용
        SecureRandom random = new SecureRandom();

        // 각 카테고리에서 최소 한 번 이상 선택되도록 랜덤 문자열 생성
        randomStringBuilder.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        randomStringBuilder.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        randomStringBuilder.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

        // 남은 길이에 대해 랜덤 문자열 생성
        for (int i = 3; i < length; i++) {
            // characters 문자열에서 랜덤한 인덱스를 얻어서 해당 문자를 StringBuilder에 추가
            String category = selectCategory(i);
            randomStringBuilder.append(category.charAt(random.nextInt(category.length())));
        }

        // 문자열 섞기
        for (int i = randomStringBuilder.length() - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = randomStringBuilder.charAt(index);
            randomStringBuilder.setCharAt(index, randomStringBuilder.charAt(i));
            randomStringBuilder.setCharAt(i, temp);
        }

        return randomStringBuilder.toString();
    }

    // 글자 카테고리 선택
    private static String selectCategory(int index) {
        // 길이 3까지는 앞서 각 카테고리에서 이미 하나씩 선택되었으므로 이를 고려하지 않음
        if (index < 3) return null;
        // index를 3으로 나눈 나머지에 따라 카테고리 선택
        int categoryIndex = index % 3;
        switch (categoryIndex) {
            case 0:
                return LETTERS;
            case 1:
                return NUMBERS;
            case 2:
                return SPECIAL_CHARACTERS;
            default:
                return null;
        }
    }
}
