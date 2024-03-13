package com.example.springjwt.dto;

import com.example.springjwt.entity.LoginHistoryEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LoginHistoryDTO {
    private String loginId;
    private LocalDateTime loginDt;
    private String loginIp;
    private String sucessYn;

    public LoginHistoryDTO(LoginHistoryEntity historyEntity) {
        this.loginId = historyEntity.getLoginId();
        this.loginDt = historyEntity.getLoginDt();
        this.loginIp = historyEntity.getLoginIp();
        this.sucessYn = historyEntity.getSucessYn();
    }
}
