package com.learn.knowledgesystem.service;

import com.learn.knowledgesystem.dto.PasswordChangeDTO;
import com.learn.knowledgesystem.dto.UserProfileDTO;
import com.learn.knowledgesystem.entity.User;
import com.learn.knowledgesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${app.upload.avatar-dir}")
    private String avatarDir;

    public User updateProfile(Long userId, UserProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        return userRepository.save(user);
    }

    public void changePassword(Long userId, PasswordChangeDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        try {
            // 使用绝对路径
            File dir = new File(avatarDir);  // 直接使用成员变量
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("创建上传目录: " + dir.getAbsolutePath() + " " + (created ? "成功" : "失败"));
            }
            if (!dir.canWrite()) {
                System.out.println("上传目录不可写: " + dir.getAbsolutePath());
            }

            String suffix = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String filename = userId + "_" + System.currentTimeMillis() + suffix;
            File dest = new File(dir, filename);
            System.out.println("目标文件绝对路径: " + dest.getAbsolutePath());

            file.transferTo(dest);

            // 生成可访问的 URL（应与静态资源映射一致）
            String avatarUrl = "/uploads/avatars/" + filename;
            System.out.println("头像URL: " + avatarUrl);

            // 更新用户头像
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("头像上传失败", e);
        }
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
