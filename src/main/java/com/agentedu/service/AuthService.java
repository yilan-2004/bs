package com.agentedu.service;

import com.agentedu.dto.LoginDTO;
import com.agentedu.dto.RegisterDTO;
import com.agentedu.vo.LoginVO;
import com.agentedu.vo.UserVO;

public interface AuthService {

    /**
     * 用户注册，返回新用户ID。
     */
    Long register(RegisterDTO dto);

    /**
     * 用户登录，登录成功后返回Token和用户基础信息。
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户信息。
     */
    UserVO getCurrentUserInfo();

    /**
     * 退出当前登录会话。
     */
    void logout();
}
