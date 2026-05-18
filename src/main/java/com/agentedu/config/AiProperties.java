package com.agentedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 是否启用真实 AI 服务；关闭时使用 MockAiModelClient。
     */
    private Boolean enabled = false;

    /**
     * AI 服务提供商，第一版支持 deepseek。
     */
    private String provider = "mock";

    /**
     * AI 服务基础地址，不包含具体接口路径。
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 模型名称。
     */
    private String model = "deepseek-chat";
}
