package com.agentedu.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long userId;

    private String username;

    private String realName;

    private String role;

    private String email;

    private String phone;

    private Integer status;
}
