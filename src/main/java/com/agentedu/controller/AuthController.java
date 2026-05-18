package com.agentedu.controller;

import com.agentedu.common.Result;
import com.agentedu.dto.LoginDTO;
import com.agentedu.dto.RegisterDTO;
import com.agentedu.service.AuthService;
import com.agentedu.vo.LoginVO;
import com.agentedu.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录，成功后返回Sa-Token令牌和用户基础信息。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 用户注册，密码会在服务层加密后保存。
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 退出当前登录会话。
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
