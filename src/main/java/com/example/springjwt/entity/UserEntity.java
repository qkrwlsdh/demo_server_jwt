package com.example.springjwt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Data
@Table(name = "TUSER")
@ToString
public class UserEntity {

    @Id
    @Column(unique = true, length = 20)
    private String loginId;

    @Column(length = 100)
    private String loginPw;

    private long failCnt;

    @Column(length = 2)
    private String lockYn;

    @Column(length = 50)
    private String googleOtp;

    private String role;
}
