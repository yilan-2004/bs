package com.agentedu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "judge")
public class JudgeProperties {

    private String pythonCommand = "python";

    private String javacCommand = "javac";

    private String javaCommand = "java";

    private Long timeoutSeconds = 3L;

    private Integer maxCodeLength = 51200;

    private Integer maxOutputLength = 10240;

    private String tempDir = "temp_code";
}
