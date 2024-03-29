package com.example.springjwt.dto;

import com.example.springjwt.entity.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String loginId;
    private String loginPw;
    private long failCnt;
    private String lockYn;
    private String googleOtp;
    private String ccNo;
    private String repBirthDt;
    private String email;
    private String role;

    public static UserDTO toMemberDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setLoginId(userEntity.getLoginId());
        userDTO.setLoginPw(userEntity.getLoginPw());
        userDTO.setLockYn(userEntity.getLockYn());
        userDTO.setFailCnt(userEntity.getFailCnt());
        userDTO.setGoogleOtp(userEntity.getGoogleOtp());
        userDTO.setCcNo(userEntity.getCcNo());
        userDTO.setRepBirthDt(userEntity.getRepBirthDt());
        userDTO.setEmail(userEntity.getEmail());
        userDTO.setRole(userEntity.getRole());
        return userDTO;
    }
}
