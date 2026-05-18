package com.agentedu.config;

import com.agentedu.service.AiModelClient;
import com.agentedu.service.PromptBuilder;
import com.agentedu.service.impl.DeepSeekAiModelClient;
import com.agentedu.service.impl.MockAiModelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AiModelClientConfig {

    private final AiProperties aiProperties;

    private final PromptBuilder promptBuilder;

    private final ObjectMapper objectMapper;

    /**
     * 根据配置选择真实 DeepSeek 客户端或 Mock 客户端。
     */
    @Bean
    public AiModelClient aiModelClient() {
        if (Boolean.TRUE.equals(aiProperties.getEnabled())
                && "deepseek".equalsIgnoreCase(aiProperties.getProvider())) {
            return new DeepSeekAiModelClient(aiProperties, promptBuilder, objectMapper);
        }
        return new MockAiModelClient();
    }
}
