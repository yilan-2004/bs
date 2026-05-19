package com.agentedu.vo.avatar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterAvatarVO {
    private String id;
    private String name;
    private String description;
    private String defaultAvatarModelId;
    private List<AvatarModelVO> avatarModels;
}
