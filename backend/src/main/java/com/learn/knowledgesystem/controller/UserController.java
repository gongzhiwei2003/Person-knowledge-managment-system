package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.dto.PasswordChangeDTO;
import com.learn.knowledgesystem.dto.UserProfileDTO;
import com.learn.knowledgesystem.entity.User;
import com.learn.knowledgesystem.service.UserService;
import com.learn.knowledgesystem.util.JwtUtil;
import com.learn.knowledgesystem.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;  // 注入 PasswordEncoder
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody User user) {
// 检查用户是否存在
        if (userService.findByUsername(user.getUsername()) != null) {
            return ApiResponse.error(400, "用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 这里应该添加密码加密等逻辑
        userService.saveUser(user);
        return ApiResponse.success("注册成功", user.getUsername());
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        User user = userService.findByUsername(username);
        if (user == null) {
            return ApiResponse.error(400, "用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ApiResponse.error(400, "密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        return ApiResponse.success("登录成功", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ApiResponse.success(user);
    }

    // 获取当前用户信息
    @GetMapping("/profile")
    public ApiResponse<User> getProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userService.findById(userId);
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    // 更新个人资料
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody UserProfileDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        User updated = userService.updateProfile(userId, dto);
        updated.setPassword(null);
        return ApiResponse.success("更新成功", updated);
    }

    // 修改密码
    @PutMapping("/password")
    public ApiResponse<?> changePassword(@RequestBody PasswordChangeDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.changePassword(userId, dto);
        return ApiResponse.success("密码修改成功", null);
    }

    // 上传头像
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        String avatarUrl = userService.uploadAvatar(userId, file);
        return ApiResponse.success("头像上传成功", avatarUrl);
    }


}
