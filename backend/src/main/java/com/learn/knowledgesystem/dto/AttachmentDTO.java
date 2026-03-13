package com.learn.knowledgesystem.dto;

import lombok.Data;

@Data
public class AttachmentDTO {
    private String type;      // "file", "video", "link"
    private String name;      // 显示名称
    private String url;       // 访问地址（对文件为下载接口地址，对链接为外部URL）
    private Long size;        // 文件大小（可选）
}