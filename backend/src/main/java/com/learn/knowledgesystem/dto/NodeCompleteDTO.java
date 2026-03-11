package com.learn.knowledgesystem.dto;

import lombok.Data;

@Data
public class NodeCompleteDTO {
    private Long roadmapId;
    private String nodeId;       // 对应 graphData 中的节点 id
    private boolean completed;
}