package com.agentedu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeDocumentUpdateDTO {

    @NotNull(message = "文档ID不能为空")
    private Long id;

    @NotNull(message = "知识库ID不能为空")
    private Long baseId;

    @NotBlank(message = "文档标题不能为空")
    private String title;

    @NotBlank(message = "文档内容不能为空")
    private String content;

    private String knowledgeTags;
}
