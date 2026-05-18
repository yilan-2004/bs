package com.agentedu.dto;

import lombok.Data;

@Data
public class AgentAskDTO {

    /**
     * 学生在 AI 助教页输入的学习问题。
     */
    private String question;
}
