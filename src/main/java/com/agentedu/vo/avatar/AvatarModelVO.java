package com.agentedu.vo.avatar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AIRI-style avatar model definition.
 *
 * One character can expose multiple avatarModels. Each model keeps a stable
 * renderer type plus a config map, so frontend code does not need a dedicated
 * endpoint per renderer such as a Live2D-only API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarModelVO {
    private String id;
    private String name;
    private String type;
    private String description;
    private Map<String, AvatarModelSourceVO> config;
    private AvatarLayoutVO layout;
    private Map<String, AvatarStateVO> states;
}
