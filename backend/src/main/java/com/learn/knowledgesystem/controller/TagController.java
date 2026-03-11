package com.learn.knowledgesystem.controller;

import com.learn.knowledgesystem.common.ApiResponse;
import com.learn.knowledgesystem.dto.TagDTO;
import com.learn.knowledgesystem.entity.Tag;
import com.learn.knowledgesystem.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ApiResponse<List<TagDTO>> getAllTags() {
        return ApiResponse.success(tagService.getAllTagDTOs());
    }

    @GetMapping("/{id}")
    public ApiResponse<Tag> getTag(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "标签不存在"));
    }

    @PostMapping
    public ApiResponse<Tag> createTag(@RequestBody TagDTO tagDTO) {
        if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            return ApiResponse.error(400, "标签名称不能为空");
        }
        // 检查名称是否已存在
        if (tagService.findByName(tagDTO.getName()).isPresent()) {
            return ApiResponse.error(400, "标签已存在");
        }
        Tag saved = tagService.createTag(tagDTO.getName());
        return ApiResponse.success("创建成功", saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Tag> updateTag(@PathVariable Long id, @RequestBody TagDTO tagDTO) {
        if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            return ApiResponse.error(400, "标签名称不能为空");
        }
        // 检查是否存在其他同名标签（排除自身）
        tagService.findByName(tagDTO.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("标签名称已存在");
            }
        });
        Tag updated = tagService.updateTag(id, tagDTO.getName()); // 建议 service 提供按 id 和 name 更新的方法
        return ApiResponse.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.success("删除成功", null);
    }

}