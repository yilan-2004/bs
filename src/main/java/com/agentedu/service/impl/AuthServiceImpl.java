package com.agentedu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.agentedu.dto.LoginDTO;
import com.agentedu.dto.RegisterDTO;
import com.agentedu.entity.User;
import com.agentedu.enums.UserRoleEnum;
import com.agentedu.exception.BusinessException;
import com.agentedu.mapper.UserMapper;
import com.agentedu.service.AuthService;
import com.agentedu.vo.LoginVO;
import com.agentedu.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Long register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        validatePassword(dto.getPassword());
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(UserRoleEnum.STUDENT.name());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        StpUtil.login(user.getId());
        StpUtil.getSession().set("role", user.getRole());
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("realName", user.getRealName());

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public UserVO getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("当前登录用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setEmail(maskEmail(user.getEmail()));
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setStatus(user.getStatus());
        return vo;
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8 || password.length() > 32) {
            throw new BusinessException("密码长度需为 8-32 位，并包含字母和数字");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException("密码需同时包含字母和数字");
        }
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String prefix = name.length() <= 2 ? name.substring(0, 1) : name.substring(0, 2);
        return prefix + "***" + email.substring(at);
    }
}
