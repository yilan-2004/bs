package com.agentedu.vo.avatar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarStateVO {
    private String motionGroup;
    private String expression;
    private String message;
}
