package com.agentedu.vo.avatar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarLayoutVO {
    private Integer width;
    private Integer height;
    private Double scale;
    private Integer x;
    private Integer y;
    private Boolean transparent;
    private Boolean followPointer;
}
