package com.agentedu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_case")
public class TestCase {

    private Long id;

    private Long problemId;

    private String inputData;

    private String expectedOutput;

    private Integer isSample;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
